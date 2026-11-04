# Architecture

How the services are meant to work together (target ECS deployment).

![Architecture](architecture.png)

```mermaid
flowchart LR
    client["Frontend Client"]

    subgraph internet["Internet"]
        client
    end

    subgraph vpc["VPC"]
        alb["Application Load Balancer<br/>(public subnet)"]

        subgraph ecs["ECS Cluster (private subnet)"]
            subgraph s_gw["ECS Service"]
                gw["API Gateway<br/>(ECS task) :4004<br/>JwtValidation filter"]
            end
            subgraph s_auth["ECS Service"]
                auth["Auth Service<br/>(ECS task) :4005"]
            end
            subgraph s_patient["ECS Service"]
                patient["Patient Service<br/>(ECS task) :4000<br/>gRPC client + Kafka producer"]
            end
            subgraph s_billing["ECS Service"]
                billing["Billing Service<br/>(ECS task) :4001<br/>gRPC server :9001"]
            end
            subgraph s_analytics["ECS Service"]
                analytics["Analytics Service<br/>(ECS task) :4002<br/>Kafka consumer"]
            end
        end

        subgraph rds["RDS (private subnet)"]
            authdb[("Auth Service DB")]
            patientdb[("Patient Service DB")]
        end

        subgraph msk["MSK (private subnet)"]
            topic[["Kafka topic: patient"]]
        end
    end

    client -->|HTTP| alb
    alb --> gw
    gw -->|"/api/v1/auth/**"| auth
    gw -->|"/api/v1/patients/** (token required)"| patient
    gw -.->|"GET /api/v1/auth/validate"| auth
    auth --> authdb
    patient --> patientdb
    patient -->|"gRPC: CreateBillingAccount"| billing
    patient -.->|"publish PatientEvent (protobuf)"| topic
    topic -.->|"consume PatientEvent"| analytics
```

## Request flow: login, then create a patient

![Create patient sequence](create-patient-sequence.png)

## Notes

- Only the API Gateway is exposed (via the load balancer). Auth, Patient, Billing and Analytics are internal.
- The gateway's `JwtValidation` filter calls `GET /api/v1/auth/validate` before forwarding `/api/v1/patients/**`. `/api/v1/auth/**` is open so users can log in.
- Creating a patient: save to the Patient DB, call Billing over gRPC, then publish a `PatientEvent` to the `patient` Kafka topic. Analytics consumes it.
- Running locally with Docker Compose, the same services run as containers on the `internal` network (gateway on `localhost:4004`, Postgres on 5001/5002, Kafka on 9092/9094). RDS, ECS, the load balancer and MSK in the diagram are replaced by Compose services.
- Source files: `architecture.mmd` and `create-patient-sequence.mmd`. Re-render with `mmdc -i <file>.mmd -o <file>.png -s 2`.
