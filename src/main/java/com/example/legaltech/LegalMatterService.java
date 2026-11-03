package com.example.legaltech;

import java.io.IOException;
import java.util.List;

public final class LegalMatterService {
    public record Matter(String id, String client, String email, String signedFile, int daysUntilDeadline) {}
    public record Notice(String kind, String subject, String body) {}

    public List<Notice> noticesFor(Matter matter) {
        return List.of(
                new Notice("matter-intake", "Matter received: " + matter.id(), "We opened the matter for " + matter.client() + "."),
                new Notice("signed-document", "Signed document ready", "Your signed document is ready: " + matter.signedFile() + "."),
                new Notice("deadline-follow-up", "Deadline follow-up", matter.daysUntilDeadline() <= 3 ? "Your legal deadline is in " + matter.daysUntilDeadline() + " days." : "We will follow up before the legal deadline.")
        );
    }

    public String onboardAndNotify(String domain, Matter matter, InfraiClient client) throws IOException, InterruptedException {
        client.verifyDnsDomain(domain);
        String verification = client.verifyEmailDomain(domain);
        for (Notice notice : noticesFor(matter)) client.send(matter.email(), notice.subject(), notice.body());
        return verification;
    }
}
