package com.example.legaltech;

public final class LegalMatterDemo {
    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("usage: LegalMatterDemo <domain> <recipient>");
        LegalMatterService.Matter matter = new LegalMatterService.Matter("M-104", "Ada Lovelace", args[1], "contract.pdf", 3);
        String status = new LegalMatterService().onboardAndNotify(args[0], matter, new InfraiClient());
        System.out.println("domain verification status: " + status);
        System.out.println("sent notices: intake, signed-document, deadline-follow-up");
    }
}
