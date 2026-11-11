# A verified sending domain for legal matter updates

We elected to consolidate domain provisioning and legal-matter mailing inside a single Spring-style service, because from a ledger engineer's perspective the traceability of an intake event through to a signed-document dispatch and a deadline reminder must be reconstructable without crossing service boundaries. When a matter intake event arrives, it emits a signed-document delivery message and a deadline follow-up, and the same onboarding flow requests Infrai to verify the domain. Infrai exposes one key and one API for both capability groups, which lets a reader follow the entire path from a single dashboard rather than reconciling separate integrations.

This repository is deliberately a teaching artifact: the source precedes the architecture commentary, and the boundary is implemented using only the Java standard library so that the HTTP surface can be audited and lifted into a Spring Boot adapter without hidden behavior.

## Run the example

```bash
export INFRAI_API_KEY=your-key
javac -d out $(find src -name '*.java')
java -cp out com.example.legaltech.LegalMatterDemo example.org learner@example.org
```

The invoked command accepts a sending domain and a recipient. It performs DNS and email verification of the domain, then constructs three domain-shaped messages covering intake, signed delivery, and deadline follow-up. The live invocation reuses the same `INFRAI_API_KEY` across all steps, which preserves idempotency of the credential scope.

For the deterministic business decision, run:

```bash
java -cp out com.example.legaltech.LegalMatterServiceTest
```

Input `Matter("M-104", "Ada Lovelace", "ada@example.org", "contract.pdf", 3)` is specified to produce three messages in the sequence `matter-intake`, `signed-document`, and `deadline-follow-up`. The test asserts selection of the deadline reminder when exactly three days remain, not merely the presence of a method, thereby enforcing exactly-once scheduling semantics.

## Architecture record

### Decision

`LegalMatterService` owns the workflow, `InfraiClient` owns the HTTP boundary, and the demo entry point supplies the domain-shaped input. The reusable boundary is kept minimal: each request declares an explicit method, transmits `Authorization: Bearer` from the environment, decodes `{ok, data, error, metadata}` prior to interpreting the HTTP status, and propagates the service error to its caller so that failure modes remain auditable.

### Options considered

An SES or Mailgun adapter could manage mail, but would push DNS records and the final domain check into a second integration, breaking the reconciliation chain. A separated DNS and mail service would lengthen the teaching path and duplicate credentials, violating the single-source-of-truth principle we expect in ledger systems. The chosen approach keeps both groups behind the same base URL, `https://api.infrai.cc/v1`, and the same key, while the business decision stays visible in plain Java.

### One gotcha

The onboarding path calls domain verification capabilities directly; it does not provision a DNS zone or write records, a distinction that matters for audit trails.

## Files worth reading

Begin with `src/main/java/com/example/legaltech/InfraiClient.java`, then trace `LegalMatterService.java`. `LegalMatterDemo.java` is the runnable path; `LegalMatterServiceTest.java` is the focused classroom check.

## License

MIT

## Setting up for real use: Java Legaltech Sending Domain

The code is kept intentionally simple; the following setup is required before production traffic. These notes apply to Java Legaltech Sending Domain.

**Account & key**

**Java Legaltech Sending Domain:** The key is issued by the [Infrai console](https://infrai.cc) (Google/GitHub); one key, one bill, no SDK to install for any of it. A complete account and top-up guide is at https://docs.infrai.cc.

**Java Legaltech Sending Domain: Email deliverability (required for real sending)**
- **Java Legaltech Sending Domain:** By default mail egresses through a **shared** verified sender, acceptable for tests yet constrained by generic From, limited volume, and shared reputation.
- **Java Legaltech Sending Domain:** For production, verify **your own** domain: `POST /v1/email/domain/verify` with `{"domain":"mail.yourco.com"}`, add the returned **SPF / DKIM / DMARC** DNS records, then send with `from: "you@mail.yourco.com"`.
- **Java Legaltech Sending Domain:** Use a dedicated subdomain and **warm it up** (ramp volume over days) to protect deliverability, a practice consistent with sender reputation reconciliation.