#!/usr/bin/env bash

# Stop the script immediately if any command fails, instead of carrying on.
set -euo pipefail

# ---------- Settings (change these if needed) ----------

# LocalStack listens on this address on your machine.
ENDPOINT_URL="http://localhost:4566"

# The (fake) AWS region LocalStack should use.
REGION="us-east-1"

# The name of the CloudFormation stack that will be created.
STACK_NAME="patient-management"

# The template that "cdk synth" generated from your Java code.
TEMPLATE_FILE="./cdk.out/localstack.template.json"

# If a .env file exists next to this script, load the variables from it.
# "source" runs the file as part of this script, so DB_PASSWORD=... in it
# becomes available below.
ENV_FILE="$(dirname "$0")/.env"
if [[ -f "$ENV_FILE" ]]; then
    # shellcheck source=/dev/null
    source "$ENV_FILE"
fi

# ---------- Helper ----------

# Every aws command below needs the same options, so we define them once.
# Running "awscli something" is the same as running:
#   aws --endpoint-url=... --region ... --no-cli-pager something
# The --endpoint-url option is what points the aws command at LocalStack
# instead of real AWS.
awscli() {
    aws --endpoint-url="$ENDPOINT_URL" --region "$REGION" --no-cli-pager "$@"
}

# ---------- Step 1: remove any old version of the stack ----------

# Delete stack
echo "Removing the old stack (if there is one)..."
awscli cloudformation delete-stack --stack-name "$STACK_NAME" || true
awscli cloudformation wait stack-delete-complete --stack-name "$STACK_NAME" || true

# ---------- Step 2: deploy the stack ----------

echo "Deploying the stack..."

# "if ! command; then" runs the commands inside only when the deploy FAILS.
if ! awscli cloudformation deploy \
    --stack-name "$STACK_NAME" \
    --template-file "$TEMPLATE_FILE" \
    --parameter-overrides "DbPassword=$DB_PASSWORD"; then

    # Show only the events that failed. They are listed oldest first,
    # so the first row is the root cause.
    echo "Deployment failed. These are the failed events:"
    awscli cloudformation describe-stack-events \
        --stack-name "$STACK_NAME" \
        --query "reverse(StackEvents[?contains(ResourceStatus, 'FAILED')].[LogicalResourceId,ResourceStatusReason])" \
        --output table || true

    exit 1
fi

# ---------- Done ----------

echo "Deployment succeeded."

# The elbv2 API needs LocalStack Pro, so instead we list the load balancer
# that CloudFormation recorded in the stack. "|| true" stops the script
# from failing if the lookup doesn't work.
echo "Load balancer resource in the stack:"
awscli cloudformation describe-stack-resources \
    --stack-name "$STACK_NAME" \
    --query "StackResources[?ResourceType=='AWS::ElasticLoadBalancingV2::LoadBalancer'].[LogicalResourceId,PhysicalResourceId,ResourceStatus]" \
    --output table || true

echo "Your API gateway is running in Docker at: http://localhost:4004"