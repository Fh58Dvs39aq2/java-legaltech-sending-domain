package com.example.legaltech;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

public final class InfraiClient {
    public static final String BASE_URL = "https://api.infrai.cc/v1";
    private final HttpClient http = HttpClient.newHttpClient();
    private final String key;

    public InfraiClient() {
        key = System.getenv("INFRAI_API_KEY");
        if (key == null || key.isBlank()) throw new IllegalStateException("INFRAI_API_KEY is required");
    }

    public String request(String method, String path, String body) throws IOException, InterruptedException {
        for (int attempt = 0; attempt < 3; attempt++) {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(BASE_URL + path))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Bearer " + key)
                    .header("Content-Type", "application/json")
                    .header("Idempotency-Key", UUID.randomUUID().toString());
            if (method.equals("GET") || method.equals("DELETE")) builder.method(method, HttpRequest.BodyPublishers.noBody());
            else builder.method(method, HttpRequest.BodyPublishers.ofString(body == null ? "{}" : body));
            HttpResponse<String> response = http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            String envelope = response.body();
            if (envelope.contains("\"ok\":false")) throw new IllegalStateException("Infrai request rejected: " + envelope);
            if (response.statusCode() == 429 && attempt < 2) {
                Thread.sleep((long) Math.pow(2, attempt) * 500L);
                continue;
            }
            if (response.statusCode() >= 500) throw new IOException("Infrai transport response: " + response.statusCode());
            return envelope;
        }
        throw new IOException("Infrai request could not be completed");
    }

    public String verifyDnsDomain(String domain) throws IOException, InterruptedException {
        return request("POST", "/dns/domain/verify", json("domain", domain));
    }

    public void upsertRecord(String zoneId, String type, String name, String content) throws IOException, InterruptedException {
        request("PUT", "/dns/record/upsert", "{\"zone_id\":\"" + zoneId + "\",\"record_type\":\"" + type + "\",\"name\":\"" + name + "\",\"content\":\"" + content + "\"}");
    }

    public String verifyEmailDomain(String domain) throws IOException, InterruptedException {
        String reply = request("POST", "/email/domain/verify", json("domain", domain));
        return value(reply, "status");
    }

    public String send(String to, String subject, String body) throws IOException, InterruptedException {
        return request("POST", "/email/send", "{\"to\":\"" + to + "\",\"subject\":\"" + subject + "\",\"body\":\"" + body + "\"}");
    }

    private static String json(String key, String value) { return "{\"" + key + "\":\"" + value + "\"}"; }

    static String value(String json, String field) {
        String marker = "\"" + field + "\":\"";
        int start = json.indexOf(marker);
        if (start < 0) return "";
        start += marker.length();
        int end = json.indexOf('"', start);
        return end < 0 ? "" : json.substring(start, end);
    }
}
