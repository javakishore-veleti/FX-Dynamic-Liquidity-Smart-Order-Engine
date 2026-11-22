# CHANGES_0003 — First AWS run of the topology stack

**Change ID:** CHANGES_0003
**Date opened:** 2026-11-22
**Moved from:** [CHANGES_0002_AWS_GH_Actions.md](CHANGES_0002_AWS_GH_Actions.md) §2 E7 and §3 F7.1, F7.2
**Status key:** `DONE` · `TODO` · `BLOCKED` (on an external input only) · `DEFERRED`

The first AWS run is no longer a blocker for the coding work in CHANGES_0002. Coding is complete there; this file tracks running it.

## Features

| Feature | User story | Acceptance criteria | Status | Start | Target | End |
|---|---|---|---|---|---|---|
| F3.1 Set GitHub secrets | As the owner, I want the AWS credentials and region stored as GitHub secrets, so workflows can authenticate. | `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION` exist. VPC secrets only if reusing a VPC. | TODO (owner) | | | |
| F3.2 Cost approval | As the owner, I want the expected cost stated before the run, so I can approve it. | CHANGES_0002 §6.7 cost section reviewed. Owner approves. | TODO (owner) | | | |
| F3.3 First setup run | As the owner, I want `AWS-0100-Setup-All` to build the stack once. | Run finishes green. Each stack reaches CREATE_COMPLETE. | TODO | | | |
| F3.4 Health check | As the owner, I want the load balancer to answer the health endpoint. | `http://<alb-dns>/actuator/health` returns 200. | TODO | | | |
| F3.5 Teardown | As the owner, I want everything removed afterwards so cost stops. | `AWS-0100-Destroy-All` finishes and no fx-topology stack remains. | TODO | | | |

## Notes
- Nothing in this file is run by the assistant without owner approval.
- Any problem found during the first run gets its own row here, so the fix is tracked.
