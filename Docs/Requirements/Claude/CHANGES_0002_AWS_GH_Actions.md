# CHANGES_0002 — Deploy fx-topology to AWS with GitHub Actions and CloudFormation

**Change ID:** CHANGES_0002
**Requested by:** Repo owner
**Date opened:** 2026-11-22
**Last updated:** 2026-11-22
**Branch of record:** `main`
**Related:** [CHANGE_001.md](CHANGE_001.md) (E10 topology premises), [TOPOLOGY-PREMISES.md](TOPOLOGY-PREMISES.md)
**Rules applied:** [DESIGN.md](../../../DESIGN.md) §1 (zero sharing, closed layers), §7 (git rules), §8 (docs rules); [ARCHITECTURE.md](../../../ARCHITECTURE.md)

**Status key:** `DONE` · `IN PROGRESS` · `TODO` · `BLOCKED` · `NOT STARTED`
**Dates:** `Start` is when work began. `Target` is the planned end. `End` is filled when the item is done.

---

## 1. Scope and decisions

| # | Decision | Detail |
|---|---|---|
| D1 | Service deployed | `fx-topology-service` only (port 8085). Other middleware services are out of scope for this change. |
| D2 | Local first | Run the service locally with the runbook commands. Verify health before any AWS work. |
| D3 | Cloud | AWS, managed services only: ECS Fargate, RDS PostgreSQL, ECR, Application Load Balancer, Secrets Manager, CloudWatch Logs, CloudFormation. |
| D4 | Infrastructure as code | One CloudFormation template per aspect, under `CICD/AWS/cfn/`. Each stack has its own name and its own GitHub workflow. |
| D5 | Triggers | Every workflow is `workflow_dispatch` (manual). No push or schedule triggers. |
| D6 | Naming | Setup: `AWS-0001-Setup-<aspect>.yml`. Destroy: `AWS-0001-Destroy-<aspect>.yml`. Orchestrators: `AWS-0100-Setup-All.yml` and `AWS-0100-Destroy-All.yml`. |
| D7 | VPC reuse | Optional input on the network setup run: `use_existing_vpc` (`no` by default). When `yes`, the run reuses the VPC and subnets held in GitHub secrets. |
| D8 | Secrets | AWS credentials and the VPC id live in GitHub Actions secrets. No credential or account id is committed to the repo. |
| D9 | Database password | Managed by RDS and stored in Secrets Manager. The password never appears in a template, a parameter, or a workflow log. |
| D10 | Orchestration | `AWS-0100-*` calls the modular workflows with `uses:` in order. Destroy runs in reverse order. |
| D11 | Destroy safety | Every destroy workflow requires the input `confirm` to equal `DESTROY`. |
| D12 | Cost | Running the setup creates billable resources (RDS, Fargate, ALB). No AWS run starts without the owner's approval. |

### GitHub secrets (to be created by the owner)

| Secret | Required | Used by |
|---|---|---|
| `AWS_ACCESS_KEY_ID` | Yes | All workflows |
| `AWS_SECRET_ACCESS_KEY` | Yes | All workflows |
| `AWS_REGION` | Yes | All workflows |
| `VPC_ID` | Only when reusing a VPC | Network setup |
| `REUSE_PUBLIC_SUBNET_A` | Only when reusing a VPC | Network setup |
| `REUSE_PUBLIC_SUBNET_B` | Only when reusing a VPC | Network setup |

---

## 2. Epics

| Epic | Name | Summary | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| E1 | Local deployment | Run fx-topology locally and confirm it is healthy | DONE | 2026-11-22 | 2026-11-22 | 2026-11-22 |
| E2 | Stack templates | CloudFormation templates for network, data, registry, and compute | IN PROGRESS | 2026-11-22 | 2026-11-22 | |
| E3 | Setup workflows | One manual GitHub workflow per stack, plus the image build | NOT STARTED | | 2026-11-22 | |
| E4 | Destroy workflows | One manual destroy workflow per stack, with confirmation | NOT STARTED | | 2026-11-22 | |
| E5 | Orchestrators | `AWS-0100-Setup-All` and `AWS-0100-Destroy-All` | NOT STARTED | | 2026-11-22 | |
| E6 | Secrets and VPC reuse | Secrets wiring and the `use_existing_vpc` input | NOT STARTED | | 2026-11-22 | |
| E7 | AWS verification | First real run in AWS, after owner approval | BLOCKED (owner approval, secrets) | | | |

---

## 3. Features and user stories

### E1 — Local deployment

| Feature | User story | Acceptance criteria | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| F1.1 Run topology locally | As the owner, I want the topology service running on my machine, so that I can confirm it before anything goes to AWS. | `GET /actuator/health` returns 200 on port 8085 after `npm run local:middleware:all:start`. | DONE | 2026-11-22 | 2026-11-22 | 2026-11-22 |
| F1.2 Local ingest path | As the owner, I want the flow-hop ingest tested locally, so that the stream path is known to work before AWS. | Unit tests pass. The Kafka path is verified against the local broker. | IN PROGRESS | 2026-11-22 | 2026-11-22 | |

### E2 — Stack templates

| Feature | User story | Acceptance criteria | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| F2.1 Network stack | As the owner, I want one stack that creates a VPC and two public subnets, or reuses an existing VPC, so that I can run in either mode. | Template passes `cfn-lint` with no errors. In create mode it makes a VPC, two subnets, and an internet route. In reuse mode it creates only security groups in the given VPC. Outputs: VPC, subnets, security group ids. | IN PROGRESS | 2026-11-22 | 2026-11-22 | |
| F2.2 Data stack | As the owner, I want a managed PostgreSQL database in private reach, with the password kept by RDS, so that no secret is stored in the repo. | Template passes `cfn-lint`. Engine version is a supported one. Password is managed by RDS. Only the topology security group can reach port 5432. | IN PROGRESS (lint warning W3691 open: engine version `16` is deprecated for new instances) | 2026-11-22 | 2026-11-22 | |
| F2.3 Registry stack | As the owner, I want an ECR repository for the topology image, so that the build can push to it. | Template passes `cfn-lint` with no findings. Repository empties on delete, so destroy works. Scan on push is on. | DONE (draft, lint clean) | 2026-11-22 | 2026-11-22 | |
| F2.4 Compute stack | As the owner, I want an ECS Fargate service behind a load balancer, so that the topology API is reachable over HTTP. | Template passes `cfn-lint`. Service runs the image from ECR with profile `local-postgres`, the database endpoint, and the password from Secrets Manager. Health check on `/actuator/health`. Kafka disabled. | NOT STARTED | | 2026-11-22 | |

### E3 — Setup workflows

| Feature | User story | Acceptance criteria | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| F3.1 `AWS-0001-Setup-network` | As the owner, I want to start the network stack by hand and choose whether to reuse a VPC, so that I control the cost and the network. | Manual trigger. Input `use_existing_vpc` (`no` default). Reads the VPC and subnet ids from secrets when `yes`. Deploys stack `fx-topology-network`. | NOT STARTED | | 2026-11-22 | |
| F3.2 `AWS-0001-Setup-data` | As the owner, I want the database stack to pick up the network outputs automatically, so that I do not type ids. | Manual trigger. Reads outputs of `fx-topology-network` with the AWS CLI. Deploys `fx-topology-data`. | NOT STARTED | | 2026-11-22 | |
| F3.3 `AWS-0001-Setup-ecr` | As the owner, I want the registry created before the image is built. | Manual trigger. Deploys `fx-topology-ecr`. | NOT STARTED | | 2026-11-22 | |
| F3.4 `AWS-0001-Setup-app` | As the owner, I want the topology image built from the repo and pushed to ECR, so that the service has an image to run. | Manual trigger. Builds the jar with Maven, builds the Docker image from `Middleware/fx-topology-service`, pushes it with the commit sha and `latest`. | NOT STARTED | | 2026-11-22 | |
| F3.5 `AWS-0001-Setup-compute` | As the owner, I want the service and load balancer started, so that the topology API is reachable. | Manual trigger. Reads outputs of the network, data, and registry stacks. Deploys `fx-topology-compute`. Prints the load balancer address. | NOT STARTED | | 2026-11-22 | |

### E4 — Destroy workflows

| Feature | User story | Acceptance criteria | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| F4.1 `AWS-0001-Destroy-compute` | As the owner, I want to stop the service and load balancer without touching the database. | Requires `confirm` = `DESTROY`. Deletes `fx-topology-compute` and waits for completion. | NOT STARTED | | 2026-11-22 | |
| F4.2 `AWS-0001-Destroy-data` | As the owner, I want to delete the database, knowing that its data is deleted with it. | Requires `confirm` = `DESTROY`. Deletes `fx-topology-data` and waits. Owner is told the data is not kept. | NOT STARTED | | 2026-11-22 | |
| F4.3 `AWS-0001-Destroy-ecr` | As the owner, I want to delete the registry and its images. | Requires `confirm` = `DESTROY`. Deletes `fx-topology-ecr`, emptying it first. | NOT STARTED | | 2026-11-22 | |
| F4.4 `AWS-0001-Destroy-network` | As the owner, I want to delete the network stack without deleting a VPC I reused. | Requires `confirm` = `DESTROY`. Deletes `fx-topology-network`. In reuse mode the existing VPC is left as it is. | NOT STARTED | | 2026-11-22 | |

### E5 — Orchestrators

| Feature | User story | Acceptance criteria | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| F5.1 `AWS-0100-Setup-All` | As the owner, I want one manual run to build the whole topology stack in AWS, in the right order. | Calls, in order: network, data, ecr, app, compute. Passes `use_existing_vpc` through. Stops at the first failure. | NOT STARTED | | 2026-11-22 | |
| F5.2 `AWS-0100-Destroy-All` | As the owner, I want one manual run to remove everything, in reverse order. | Requires `confirm` = `DESTROY`. Calls: compute, data, ecr, network. | NOT STARTED | | 2026-11-22 | |

### E6 — Secrets and VPC reuse

| Feature | User story | Acceptance criteria | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| F6.1 Secrets documented | As the owner, I want a list of the secrets to create, so that setup is quick. | Secrets table in §1 is complete and matches the workflows. | DONE (table in this document) | 2026-11-22 | 2026-11-22 | 2026-11-22 |
| F6.2 Reuse prompt | As the owner, I want to be asked at run time whether to reuse a VPC, so that I choose per run. | Manual input `use_existing_vpc` with values `no` and `yes`. `no` is the default. | NOT STARTED | | 2026-11-22 | |
| F6.3 No secrets in code | As the owner, I want no account id, key, or password committed, so that the repo can be shared. | Repo scan finds no keys or ids. Password comes only from Secrets Manager. | NOT STARTED (check before first commit of workflows) | | 2026-11-22 | |

### E7 — AWS verification

| Feature | User story | Acceptance criteria | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| F7.1 First run in AWS | As the owner, I want the setup run once in AWS, after I approve the cost, so that I see it work. | Owner approves. Secrets exist. `AWS-0100-Setup-All` finishes. The load balancer returns health 200 on `/actuator/health`. | BLOCKED (owner approval, AWS secrets) | | | |
| F7.2 Clean teardown | As the owner, I want the run removed afterwards, so that cost stops. | `AWS-0100-Destroy-All` finishes with no stack left in CloudFormation. | BLOCKED (depends on F7.1) | | | |

---

## 4. Open items

| # | Item | Owner | Action |
|---|---|---|---|
| 1 | cfn-lint W3691 on `02-data.yaml`: engine version `16` is deprecated for new RDS instances | Assistant | Choose a supported Postgres version, then re-lint. Stopped here because of the owner's stop instruction. |
| 2 | Compute stack, all setup and destroy workflows, orchestrators | Assistant | Build after the owner says to continue. |
| 3 | Approval to create billable AWS resources | Owner | Approve before F7.1. |
| 4 | Create the GitHub secrets in §1 | Owner | Create before F7.1. |
| 5 | Kafka end-to-end run (F1.2) | Assistant | Run against the local broker. |
| 6 | Uncommitted CloudFormation drafts (`01-network.yaml`, `02-data.yaml`, `03-ecr.yaml`) were committed as they were, including the open lint item | Assistant | Fix in the next step, not in this stop. |

---

## 5. Progress log

| Date | Change | Commit |
|---|---|---|
| 2026-11-22 | Epics and user stories written; CloudFormation drafts for network, data, and registry committed as they stand | (this commit) |
