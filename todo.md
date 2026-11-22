# Security Validation TODO

## Purpose

This document is the validation plan for the security, observability and log-retention changes in this application. Every check must be executed from the validation point described in the check. A result obtained from inside the cluster must not be treated as proof of public exposure, and a result obtained from a public client must not be treated as proof of internal or VPN access.

The checkboxes intentionally start unchecked. A repository manifest, a code inspection or an assumption about the platform is not runtime validation.

## Architecture Under Evaluation

### Repositories

- Application repository: `/home/jotxee/dev/secret-santa`
- GitOps repository: `/home/jotxee/dev/gitops-config`
- Production GitOps cluster path: `clusters/pro`
- Production application path: `apps/pro/secret-santa`
- Traefik platform path: `platform/traefik`

### Runtime

- Platform: K3s
- GitOps controller: FluxCD
- Production cluster: `clusters/pro`
- Public ingress: Traefik Helm chart `42.4.1`
- Traefik namespace: `ingress`
- Application namespace: `secret-santa`
- Public hostname: `secret-santa.full4media.com`
- Public entrypoint: Traefik `websecure`
- Application Service type: `ClusterIP`
- Application container port: `8080`
- Traefik Service type: `LoadBalancer`
- Traefik `externalTrafficPolicy`: `Local`
- VPN network: `10.8.0.0/24`

### Request Flow

```text
Public client
  -> external LoadBalancer
  -> Traefik Service in namespace ingress
  -> Traefik websecure entrypoint
  -> Kubernetes Ingress
  -> Service secret-santa in namespace secret-santa
  -> Spring Boot pod
```

### Application Routes

- `/` routes to the public Secret Santa application.
- `/actuator` routes to the same backend and is intended to be VPN-only.
- `/api/**` is protected by Spring Security with the ADMIN role.
- `/.env`, `/.git/*`, `/wp-admin` and `/phpmyadmin` are handled as honeypot paths.

### Traefik Middlewares

- `ingress/internal-only` allows source range `10.8.0.0/24`.
- `ingress/rate-limit` uses average `10`, burst `20` and period `1m`.
- Secret Santa references the middlewares through the Kubernetes CRD provider.

### Trust Model

- Traefik is the only intended public ingress for the application.
- The Secret Santa Service is not intended to be directly public.
- `externalTrafficPolicy: Local` is intended to preserve the client address until Traefik.
- Traefik is responsible for sanitizing forwarded headers before forwarding requests to Spring.
- Spring is explicitly configured with `SERVER_REQUEST_HEADER_FORWARDED_STRATEGY=web-server` in the production Deployment.
- `client_ip` and `forwarded_for` must never be used as authentication or authorization evidence.
- A forwarded header can still be forged by an internal workload unless network access to the Service is restricted.

## Validation Points

### Public Client

Use a machine outside the VPN, outside the K3s cluster and outside the VPS network. This point validates exposure from the public Internet.

### VPN Client

Use a machine connected to WireGuard with a source address in `10.8.0.0/24`. This point validates the `internal-only` middleware.

### Cluster Administrator

Use a workstation or VPS with a valid `kubectl` context for the production K3s cluster. This point is required for Kubernetes resources, Flux status and pod logs.

### K3s Node

Use SSH access to the production VPS/node. This point is required to inspect node-local log storage, kubelet/containerd rotation and disk usage.

### Internal Cluster Client

Use a temporary diagnostic pod or an existing approved workload inside the cluster. This point validates access to the `ClusterIP` and tests whether an internal workload can forge forwarded headers.

### GitOps Repository

Use the checked-out GitOps repository to inspect declared configuration. Repository inspection confirms intent only; it does not prove that Flux has reconciled the configuration or that Traefik is using it at runtime.

## Reported Observations Not Independently Validated

These observations were reported by the operator during manual testing. They are recorded for context but are not marked as completed checks until the validation procedure and evidence are documented.

- The public request to `/actuator` was denied after the `internal-only` middleware was created.
- Repeated requests produced `429` from the rate limiter.

## Validation Template

Every completed check should record:

- Status: `[ ]` or `[x]`
- Validation point: public client, VPN client, cluster administrator, K3s node, internal cluster client or GitOps repository
- Executor requirements: exact access needed
- Command or action: exact procedure used
- Expected result: observable result
- Evidence: response headers, status code, logs, resource output or screenshot
- Date:
- Operator:
- Notes:

## Public Exposure

- [ ] From a public client, verify that `/actuator/metrics` is denied.
- [ ] From a VPN client, verify that the intended Actuator access works.
- [ ] From a public client, verify that `/.env` returns `404`.
- [ ] From a public client, verify that `/.git/config` returns `404`.
- [ ] Verify that no honeypot response contains file contents.
- [ ] From a cluster administrator context, verify that the Secret Santa Service remains `ClusterIP`.
- [ ] From a cluster administrator context, verify that the Service has no public `LoadBalancer` or `NodePort` exposure.
- [ ] From a public client, verify that unknown paths do not expose application files or configuration.

## Middleware And Rate Limiting

- [ ] From a public client, send a controlled burst to a safe GET endpoint.
- [ ] Record the source network, request count and time interval.
- [ ] Verify that the response eventually becomes `429`.
- [ ] From a VPN client, repeat the controlled test and document whether the same policy applies.
- [ ] From a cluster administrator context, verify that the rate-limit Middleware exists in namespace `ingress`.
- [ ] From a cluster administrator context, verify that the active Traefik configuration resolves the cross-namespace middleware reference.
- [ ] From Traefik pod logs, verify that no middleware resolution error is reported for Secret Santa.
- [ ] From a cluster administrator context, verify that the `internal-only` Middleware exists in namespace `ingress`.
- [ ] From a cluster administrator context, verify that the active Actuator route has the `internal-only` middleware attached.

## Application Security Logging

- [ ] From a public client, trigger an unauthenticated request to `/api/**`.
- [ ] From a cluster administrator context, inspect logs from the Secret Santa pod, not only Traefik logs.
- [ ] Verify an `unauthorized_api_request` event.
- [ ] Verify HTTP status `401` in that event.
- [ ] Verify request ID, method, URI, User-Agent, Referer, Accept and client IP are present.
- [ ] Verify that the application log does not contain the request query string.
- [ ] Verify that the application log does not contain cookies, passwords or Authorization credentials.
- [ ] Trigger a failed form login from a public client and verify a `failed_login_attempt` event.
- [ ] Verify that an unauthenticated API request is not mislabeled as a failed form login.
- [ ] Request a honeypot path from a public client and verify a `honeypot_request` event.
- [ ] Verify that the honeypot event returns `404` and never serves file content.
- [ ] Verify that User-Agent, Referer, Accept and URI values containing control characters cannot create additional log lines.

## Correlation IDs

- [ ] From a public client, send a request and capture the response `X-Request-ID` header.
- [ ] From a cluster administrator context, find the same request ID in the Spring application log.
- [ ] Verify that a new request receives a different request ID.
- [ ] Verify that a client-provided `X-Request-ID` cannot inject arbitrary log content or replace the server-generated ID.
- [ ] Document whether correlation between Traefik access logs and Spring logs is required.
- [ ] If cross-layer correlation is required, define where the request ID is generated and how it is propagated.

## Forwarded Headers And Client IP

- [ ] From a public client, compare the client address observed by Traefik with `client_ip` in the Spring log.
- [ ] From a VPN client, compare the VPN address observed by Traefik with `client_ip` in the Spring log.
- [ ] Verify that Traefik generates or sanitizes `X-Forwarded-For` instead of blindly copying an external value.
- [ ] From an internal cluster client, send a harmless forged `X-Forwarded-For` value to the ClusterIP and document the resulting logs.
- [ ] Verify that forwarded-header values are never used for authorization, authentication or role assignment.
- [ ] Verify that forwarded-header trust is limited to the intended deployment topology.

## Traefik Access Logs

- [ ] From a public client, request a safe URL with a harmless query marker such as `?probe=validation-only`.
- [ ] Do not use real tokens, credentials or participant data as query markers.
- [ ] From a cluster administrator context, inspect the Traefik pod logs in namespace `ingress`.
- [ ] Verify that the access log contains the request path but not `probe=validation-only`.
- [ ] Verify that query parameters are not present in the serialized `RequestPath` field.
- [ ] Verify that Authorization headers, cookies and request bodies are absent.
- [ ] Verify that only the intended request headers are retained.
- [ ] Verify that `400-599` filtering captures honeypot, unauthorized and rate-limit responses.
- [ ] Measure access-log volume during a controlled scanner simulation.
- [ ] Compare Traefik access-log volume with Spring honeypot-log volume to identify duplicate events.

## Log Storage And Retention

- [ ] From a cluster administrator context, identify the Traefik pod and node placement.
- [ ] From a K3s node, determine whether Traefik logs are only node-local or collected centrally.
- [ ] From the GitOps repository, verify whether a log collector is declared anywhere outside the Traefik configuration.
- [ ] From the K3s node, identify whether kubelet, containerd, journald or another component owns log rotation.
- [ ] Record the effective maximum log file size.
- [ ] Record the effective maximum number of rotated files.
- [ ] Record the effective retention time.
- [ ] Record the disk location used by Traefik logs.
- [ ] Record available disk space on every production node.
- [ ] Define an alert threshold for log volume.
- [ ] Define an alert threshold for node disk usage.
- [ ] Define the maximum acceptable retention period for security access logs.
- [ ] Document whether retention is controlled by K3s, kubelet, containerd, the node OS or an external logging platform.
- [ ] Decide whether access-log filtering or event sampling is required after measuring real scan volume.

## Defense In Depth

- [ ] Add application-level protection for Actuator so it does not depend solely on Traefik.
- [ ] Restrict production Actuator exposure to the minimum endpoints required, preferably only `health`.
- [ ] Evaluate a NetworkPolicy allowing application traffic only from Traefik and required internal services.
- [ ] Verify whether internal workloads can reach the Secret Santa ClusterIP.
- [ ] Verify whether internal workloads can forge forwarded headers without affecting authorization decisions.
- [ ] Re-enable CSRF protection for browser sessions and state-changing requests.
- [ ] Verify session cookie attributes: `Secure`, `HttpOnly` and `SameSite`.
- [ ] Review fallback credentials and encryption keys for non-production profiles.
- [ ] Confirm that production secrets always override application defaults.
- [ ] Confirm that `client_ip` and `forwarded_for` are never used to grant permissions or identify an authenticated user.

## Completion Evidence

- [ ] Every checked item has a validation point.
- [ ] Every checked item has an operator and date.
- [ ] Every checked item has reproducible commands or actions.
- [ ] Every checked item has recorded evidence.
- [ ] Public, VPN, cluster and node-level tests are not mixed together.
- [ ] Runtime observations are distinguished from repository assumptions.
- [ ] Log retention is documented before enabling unrestricted request logging.
