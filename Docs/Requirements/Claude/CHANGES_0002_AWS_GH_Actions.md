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
| E2 | Stack templates | CloudFormation templates for network, data, registry, and compute | DONE (lint clean) | 2026-11-22 | 2026-11-22 | 2026-11-22 |
| E3 | Setup workflows | One manual GitHub workflow per stack, plus the image build | DONE (written, YAML-validated, not run) | 2026-11-22 | 2026-11-22 | 2026-11-22 |
| E4 | Destroy workflows | One manual destroy workflow per stack, with confirmation | DONE (written, YAML-validated, not run) | 2026-11-22 | 2026-11-22 | 2026-11-22 |
| E5 | Orchestrators | `AWS-0100-Setup-All` and `AWS-0100-Destroy-All` | DONE (written, not run) | 2026-11-22 | 2026-11-22 | 2026-11-22 |
| E6 | Secrets and VPC reuse | Secrets wiring and the `use_existing_vpc` input | DONE (secrets to be created by owner) | 2026-11-22 | 2026-11-22 | 2026-11-22 |
| E7 | AWS verification | Moved to DontFocus/CHANGES_0003_AWS_First_Run.md (out of scope for this change) | MOVED | | | |

---

## 3. Features and user stories

### E1 — Local deployment

| Feature | User story | Acceptance criteria | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| F1.1 Run topology locally | As the owner, I want the topology service running on my machine, so that I can confirm it before anything goes to AWS. | `GET /actuator/health` returns 200 on port 8085 after `npm run local:middleware:all:start`. | DONE | 2026-11-22 | 2026-11-22 | 2026-11-22 |
| F1.2 Local ingest path (Kafka verified end to end) | As the owner, I want the flow-hop ingest tested locally, so that the stream path is known to work before AWS. | Unit tests pass. The Kafka path is verified against the local broker. | DONE | 2026-11-22 | 2026-11-22 | |

### E2 — Stack templates

| Feature | User story | Acceptance criteria | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| F2.1 Network stack | As the owner, I want one stack that creates a VPC and two public subnets, or reuses an existing VPC, so that I can run in either mode. | Template passes `cfn-lint` with no errors. In create mode it makes a VPC, two subnets, and an internet route. In reuse mode it creates only security groups in the given VPC. Outputs: VPC, subnets, security group ids. | DONE (lint clean) | 2026-11-22 | 2026-11-22 | |
| F2.2 Data stack | As the owner, I want a managed PostgreSQL database in private reach, with the password kept by RDS, so that no secret is stored in the repo. | Template passes `cfn-lint`. Engine version is RDS default (pinned versions flagged deprecated by cfn-lint). Password managed by RDS. Only the topology security group reaches port 5432. | DONE (lint clean) | 2026-11-22 | 2026-11-22 | 2026-11-22 |
| F2.3 Registry stack | As the owner, I want an ECR repository for the topology image, so that the build can push to it. | Template passes `cfn-lint` with no findings. Repository empties on delete, so destroy works. Scan on push is on. | DONE (draft, lint clean) | 2026-11-22 | 2026-11-22 | |
| F2.4 Compute stack | As the owner, I want an ECS Fargate service behind a load balancer, so that the topology API is reachable over HTTP. | Template passes `cfn-lint`. Service runs the image from ECR with profile `local-postgres`, the database endpoint, and the password from Secrets Manager. Health check on `/actuator/health`. Kafka disabled. | DONE (lint clean) | 2026-11-22 | 2026-11-22 | 2026-11-22 |

### E3 — Setup workflows

| Feature | User story | Acceptance criteria | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| F3.1 `AWS-0001-Setup-network` | As the owner, I want to start the network stack by hand and choose whether to reuse a VPC, so that I control the cost and the network. | Manual trigger. Input `use_existing_vpc` (`no` default). Reads the VPC and subnet ids from secrets when `yes`. Deploys stack `fx-topology-network`. | DONE (written, validated, not run) | 2026-11-22 | 2026-11-22 | 2026-11-22 | |
| F3.2 `AWS-0001-Setup-data` | As the owner, I want the database stack to pick up the network outputs automatically, so that I do not type ids. | Manual trigger. Reads outputs of `fx-topology-network` with the AWS CLI. Deploys `fx-topology-data`. | DONE (written, validated, not run) | 2026-11-22 | 2026-11-22 | 2026-11-22 | |
| F3.3 `AWS-0001-Setup-ecr` | As the owner, I want the registry created before the image is built. | Manual trigger. Deploys `fx-topology-ecr`. | DONE (written, validated, not run) | 2026-11-22 | 2026-11-22 | 2026-11-22 | |
| F3.4 `AWS-0001-Setup-app` | As the owner, I want the topology image built from the repo and pushed to ECR, so that the service has an image to run. | Manual trigger. Builds the jar with Maven, builds the Docker image from `Middleware/fx-topology-service`, pushes it with the commit sha and `latest`. | DONE (written, validated, not run) | 2026-11-22 | 2026-11-22 | 2026-11-22 | |
| F3.5 `AWS-0001-Setup-compute` | As the owner, I want the service and load balancer started, so that the topology API is reachable. | Manual trigger. Reads outputs of the network, data, and registry stacks. Deploys `fx-topology-compute`. Prints the load balancer address. | DONE (written, validated, not run) | 2026-11-22 | 2026-11-22 | 2026-11-22 | |

### E4 — Destroy workflows

| Feature | User story | Acceptance criteria | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| F4.1 `AWS-0001-Destroy-compute` | As the owner, I want to stop the service and load balancer without touching the database. | Requires `confirm` = `DESTROY`. Deletes `fx-topology-compute` and waits for completion. | DONE (written, validated, not run) | 2026-11-22 | 2026-11-22 | 2026-11-22 | |
| F4.2 `AWS-0001-Destroy-data` | As the owner, I want to delete the database, knowing that its data is deleted with it. | Requires `confirm` = `DESTROY`. Deletes `fx-topology-data` and waits. Owner is told the data is not kept. | DONE (written, validated, not run) | 2026-11-22 | 2026-11-22 | 2026-11-22 | |
| F4.3 `AWS-0001-Destroy-ecr` | As the owner, I want to delete the registry and its images. | Requires `confirm` = `DESTROY`. Deletes `fx-topology-ecr`, emptying it first. | DONE (written, validated, not run) | 2026-11-22 | 2026-11-22 | 2026-11-22 | |
| F4.4 `AWS-0001-Destroy-network` | As the owner, I want to delete the network stack without deleting a VPC I reused. | Requires `confirm` = `DESTROY`. Deletes `fx-topology-network`. In reuse mode the existing VPC is left as it is. | DONE (written, validated, not run) | 2026-11-22 | 2026-11-22 | 2026-11-22 | |

### E5 — Orchestrators

| Feature | User story | Acceptance criteria | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| F5.1 `AWS-0100-Setup-All` | As the owner, I want one manual run to build the whole topology stack in AWS, in the right order. | Calls, in order: network, data, ecr, app, compute. Passes `use_existing_vpc` through. Stops at the first failure. | DONE (written, validated, not run) | 2026-11-22 | 2026-11-22 | 2026-11-22 | |
| F5.2 `AWS-0100-Destroy-All` | As the owner, I want one manual run to remove everything, in reverse order. | Requires `confirm` = `DESTROY`. Calls: compute, data, ecr, network. | DONE (written, validated, not run) | 2026-11-22 | 2026-11-22 | 2026-11-22 | |

### E6 — Secrets and VPC reuse

| Feature | User story | Acceptance criteria | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| F6.1 Secrets documented | As the owner, I want a list of the secrets to create, so that setup is quick. | Secrets table in §1 is complete and matches the workflows. | DONE (table in this document) | 2026-11-22 | 2026-11-22 | 2026-11-22 |
| F6.2 Reuse prompt | As the owner, I want to be asked at run time whether to reuse a VPC, so that I choose per run. | Manual input `use_existing_vpc` with values `no` and `yes`. `no` is the default. | DONE (written, validated, not run) | 2026-11-22 | 2026-11-22 | 2026-11-22 | |
| F6.3 No secrets in code | As the owner, I want no account id, key, or password committed, so that the repo can be shared. | Repo scan finds no keys or ids. Password comes only from Secrets Manager. | DONE (scan clean) | 2026-11-22 | 2026-11-22 | 2026-11-22 |

### E7 — AWS verification

| Feature | User story | Acceptance criteria | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| F7.1 (moved to CHANGES_0003) First run in AWS | As the owner, I want the setup run once in AWS, after I approve the cost, so that I see it work. | Owner approves. Secrets exist. `AWS-0100-Setup-All` finishes. The load balancer returns health 200 on `/actuator/health`. | MOVED to DontFocus/CHANGES_0003 | | | |
| F7.2 (moved to CHANGES_0003) Clean teardown | As the owner, I want the run removed afterwards, so that cost stops. | `AWS-0100-Destroy-All` finishes with no stack left in CloudFormation. | MOVED to DontFocus/CHANGES_0003 | | | |

---

## 4. Open items

| # | Item | Owner | Action |
|---|---|---|---|
| 1 | cfn-lint W3691: pinned Postgres versions are flagged deprecated | Assistant | Resolved: engine version left to RDS default. Lint clean. |
| 2 | Compute stack, setup and destroy workflows, orchestrators | Assistant | DONE (written and validated, not run). |
| 3 | Approval to create billable AWS resources | Owner | Approve before F7.1. |
| 4 | Create the GitHub secrets in §1 | Owner | Create before F7.1. |
| 5 | Kafka end-to-end run (F1.2) | Assistant | Run against the local broker. |
| 6 | CloudFormation drafts committed earlier | Assistant | Fixed: all four templates lint clean. |

---

## 5. Progress log

| Date | Change | Commit |
|---|---|---|
| 2026-11-22 | Epics and user stories written; CloudFormation drafts for network, data, and registry committed as they stand | (this commit) |
| 2026-11-22 | Compute template; 11 workflows (5 setup, 4 destroy, 2 orchestrators); engine version fixed; CICD/AWS README; secrets scan clean | (this commit) |
| 2026-11-22 | Orchestrator fix: `use_existing_vpc` passed only to the network job (the only workflow declaring it) | (this commit) |

### Not run, by design
- No `AWS-0001-*` or `AWS-0100-*` workflow has been triggered. Each creates billable AWS resources, and the GitHub secrets are not yet set. Trigger `AWS-0100-Setup-All` once the owner approves.
- YAML parsing and `cfn-lint` pass. `actionlint` is not installed here, so workflow expression and step syntax are unverified beyond the parser.

---

## 6. Coding status (complete, nothing run)

All GitHub Actions and CloudFormation coding for the AWS deployment is written. Nothing has been triggered in GitHub or AWS.

### 6.1 Workflow inventory

| File | Type | Calls / depends on | Secrets used | Inputs | Validation |
|---|---|---|---|---|---|
| `AWS-0100-Setup-All.yml` | Orchestrator (manual) | network → data → ecr → app → compute | All (inherit) | `use_existing_vpc` | actionlint clean |
| `AWS-0100-Destroy-All.yml` | Orchestrator (manual) | compute → data → ecr → network | All (inherit) | `confirm` = `DESTROY` | actionlint clean |
| `AWS-0001-Setup-network.yml` | Setup, reusable | — | AWS keys and region; `VPC_ID`, `REUSE_PUBLIC_SUBNET_A`, `REUSE_PUBLIC_SUBNET_B` when reusing | `use_existing_vpc` | actionlint clean |
| `AWS-0001-Setup-data.yml` | Setup, reusable | Reads network outputs | AWS keys and region | — | actionlint clean |
| `AWS-0001-Setup-ecr.yml` | Setup, reusable | — | AWS keys and region | — | actionlint clean |
| `AWS-0001-Setup-app.yml` | Setup, reusable | Reads registry output; Maven and Docker | AWS keys and region | — | actionlint clean; Maven package verified locally |
| `AWS-0001-Setup-compute.yml` | Setup, reusable | Reads network, data, and registry outputs | AWS keys and region | — | actionlint clean |
| `AWS-0001-Destroy-compute.yml` | Destroy, reusable | — | AWS keys and region | `confirm` | actionlint clean |
| `AWS-0001-Destroy-data.yml` | Destroy, reusable | — | AWS keys and region | `confirm` | actionlint clean |
| `AWS-0001-Destroy-ecr.yml` | Destroy, reusable | — | AWS keys and region | `confirm` | actionlint clean |
| `AWS-0001-Destroy-network.yml` | Destroy, reusable | — | AWS keys and region | `confirm` | actionlint clean |

### 6.2 Templates

| File | Stack | Validation |
|---|---|---|
| `CICD/AWS/cfn/01-network.yaml` | `fx-topology-network` | cfn-lint clean |
| `CICD/AWS/cfn/02-data.yaml` | `fx-topology-data` | cfn-lint clean |
| `CICD/AWS/cfn/03-ecr.yaml` | `fx-topology-ecr` | cfn-lint clean |
| `CICD/AWS/cfn/04-compute.yaml` | `fx-topology-compute` | cfn-lint clean |
| `CICD/AWS/README.md` | — | Secrets and run order |

### 6.3 Validation performed (no AWS, no GitHub run)

| Check | Tool | Result |
|---|---|---|
| Workflow syntax, expressions, shell | actionlint (installed locally) | Clean. Shellcheck style items fixed by quoting variables |
| Templates | cfn-lint | Clean for all four |
| YAML parse | Python | All 11 workflows parse |
| Image build input | Maven package, local | Jar produced at `target/fx-topology-service-*.jar`, the path the Dockerfile copies |
| Secrets scan | grep | No keys or account ids in `CICD/AWS` or `.github` |

### 6.4 Coding decisions taken

| Decision | Reason |
|---|---|
| Postgres version left to the RDS default | cfn-lint flags pinned versions as deprecated |
| Network stack has no VPC resources in reuse mode | Security groups still go into the reused VPC; destroy does not delete the VPC |
| Orchestrator passes `use_existing_vpc` only to the network job | Only that workflow declares the input |
| Destroy workflows check `confirm` in their own first step | Each destroy is safe to run by hand, not only through the orchestrator |
| Image tagged with commit sha and `latest` | Traceable builds; compute uses `latest` |

### 6.5 Remaining (owner action only)

| Item | Owner action |
|---|---|
| F7.1 (moved to CHANGES_0003) First run in AWS | Approve cost; create the secrets in §1 |
| F7.2 (moved to CHANGES_0003) Clean teardown | Run `AWS-0100-Destroy-All` after F7.1 |

### 6.6 Progress log

| Date | Change | Commit |
|---|---|---|
| 2026-11-22 | Coding complete: 11 workflows and 4 templates, validated with actionlint, cfn-lint, YAML parse, and a local Maven package. Nothing run in AWS or GitHub | (this commit) |

### 6.7 Cost budget and decisions (first deployment)

Budget for the first run: about 30 minutes and at most $3. Decisions are chosen for cost and can be revisited after deployment.

| Resource | Decision | Why |
|---|---|---|
| NAT gateway | None. Public subnets | A NAT gateway costs more per hour than the whole first run |
| Database | RDS PostgreSQL, smallest instance class, single availability zone, 1 day of backups | Lowest cost managed database |
| Compute | One Fargate task, 0.5 vCPU, 1 GB | Smallest reasonable size for the service |
| Load balancer | One Application Load Balancer | Needed for HTTP access; the main fixed hourly cost |
| Messaging | No managed Kafka | Kafka is the most expensive piece; the ingest path is tested locally |
| Logs | 14-day retention | Keeps log storage cost near zero |
| Registry | ECR, scan on push | Storage cost is small for one image |

Estimated cost for a 30-minute run: well under $1 at on-demand list prices, before any free-tier credits. Use `AWS-0100-Destroy-All` straight after the run.

Later optimisation candidates (after the first deployment): Fargate Spot, a smaller database, a scheduled stop of the service, and a managed Kafka alternative.

### 6.8 GitHub secrets to update

| Secret | Required | Used by |
|---|---|---|
| `AWS_ACCESS_KEY_ID` | Yes | Every AWS workflow |
| `AWS_SECRET_ACCESS_KEY` | Yes | Every AWS workflow |
| `AWS_REGION` | Yes | Every AWS workflow (for example `eu-west-1`) |
| `VPC_ID` | Only when `use_existing_vpc` is `yes` | `AWS-0001-Setup-network` |
| `REUSE_PUBLIC_SUBNET_A` | Only when reusing a VPC | `AWS-0001-Setup-network` |
| `REUSE_PUBLIC_SUBNET_B` | Only when reusing a VPC | `AWS-0001-Setup-network` |

The database password is not a secret you set. RDS creates it and keeps it in Secrets Manager.


