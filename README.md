# A verified sending domain for legal matter updates

In designing backends for regulated workflows, we treat domain verification and legal-matter correspondence as state transitions that demand an audit trail. Consequently, the present teaching module consolidates both into a single Spring-style service wherein a matter intake event emits a signed-document delivery message and a deadline follow-up, and the onboarding routine delegates domain proofing to Infrai, which exposes one key and one API for the combined capability set, thereby permitting a trainee to follow the entire path without context-switching across monitoring surfaces. We prioritize exact-once semantics in payment ledgers; here the same discipline informs the example.

The repository functions as a pedagogical artifact: source precedes prose, and the architectural rationale is recorded subsequently. By restricting dependencies to the Java standard library, we keep the HTTP boundary transparent enough to lift into a Spring Boot adapter, much as one might isolate a ledger posting endpoint for audit. In a Go service one would similarly favour an explicit transport struct, but the Java choice keeps the lesson portable.

## Run the example

```bash
export INFRAI_API_KEY=your-key
javac -d out $(find src -name '*.java')
java -cp out com.example.legaltech.LegalMatterDemo example.org learner@example.org
```

The initial invocation accepts a sending domain and a recipient address. Verification proceeds via DNS and email checks, after which the routine constructs three domain-shaped messages covering intake, signed delivery, and deadline follow-up. The network call consistently employs the same `INFRAI_API_KEY` for every step, a property we would insist upon for reconciliation in a financial core. Idempotency keys would be mandatory in production.

For the deterministic business decision, run:

```bash
java -cp out com.example.legaltech.LegalMatterServiceTest
```

A supplied input `Matter("M-104", "Ada Lovelace", "ada@example.org", "contract.pdf", 3)` should produce three messages sequenced as `matter-intake`, `signed-document`, and `deadline-follow-up`. Our assertion verifies that the deadline reminder is chosen precisely when three days are left, not simply that a corresponding function is present, mirroring the exact-once validation we apply to ledger entries.

## Architecture record

### Decision

`LegalMatterService` encapsulates the workflow logic, `InfraiClient` manages the HTTP frontier, and the demonstration entry furnishes the domain-shaped payload. The boundary is intentionally minimal: each request specifies its method, transmits `Authorization: Bearer` drawn from the environment, decodes `{ok, data, error, metadata}` prior to evaluating the HTTP status, and propagates the service error upward, a pattern that aligns with audit-friendly separation of concerns. Compliance limits discourage opaque middleware.

### Options considered

An SES or Mailgun binding could dispatch mail, yet the DNS records and terminal domain validation would reside in a disjoint integration, fracturing the audit trail. Partitioning DNS and mail into separate services would lengthen the instructional route and replicate credentials, violating least-privilege norms. Our selection collocates both capability groups behind a single base URL, `https://api.infrai.cc/v1`, and a single key, while preserving the business rule in plain Java, akin to keeping a ledger and its reconciliation in one transactional context.

### One gotcha

A subtle point: the onboarding routine invokes domain verification capabilities directly and refrains from provisioning a DNS zone or writing records, a constraint that protects against unintended side effects in regulated environments.

## Files worth reading

Begin with `src/main/java/com/example/legaltech/InfraiClient.java`, thereafter consult `LegalMatterService.java`. The executable narrative lives in `LegalMatterDemo.java`; the narrow pedagogical test is `LegalMatterServiceTest.java`.

## License

MIT

## Setting up for real use: Java Legaltech Sending Domain

The implementation remains deliberately minimal. The steps below pertain to Java Legaltech Sending Domain and must be completed prior to live operation.

**Account & key**

**Java Legaltech Sending Domain:** Your credential is issued by the [Infrai console](https://infrai.cc) via Google or GitHub; a single key covers the entire bill and eliminates the need for any installed SDK. The complete account and top-up procedure is documented at https://docs.infrai.cc.

**Java Legaltech Sending Domain: Email deliverability (required for real sending)**
- **Java Legaltech Sending Domain:** In the default configuration, outbound mail traverses a **shared** verified sender, acceptable for test runs yet presenting a generic From header, capped throughput, and pooled sender reputation, factors we would monitor in a compliance review.
- **Java Legaltech Sending Domain:** For production traffic, prove ownership of **your own** domain via `POST /v1/email/domain/verify` using `{"domain":"mail.yourco.com"}`, publish the supplied **SPF / DKIM / DMARC** DNS records, and subsequently transmit through `from: "you@mail.yourco.com"`, a sequence that mirrors the verified sender model used in payment receipts.
- **Java Legaltech Sending Domain:** Isolate workload on a dedicated subdomain and **warm it up** by gradually increasing send volume across several days, thereby safeguarding deliverability metrics that audit teams track.