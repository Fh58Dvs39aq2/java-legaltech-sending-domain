package com.example.legaltech;

import java.util.List;

public final class LegalMatterServiceTest {
    public static void main(String[] args) {
        LegalMatterService.Matter matter = new LegalMatterService.Matter("M-104", "Ada Lovelace", "ada@example.org", "contract.pdf", 3);
        List<LegalMatterService.Notice> notices = new LegalMatterService().noticesFor(matter);
        if (notices.size() != 3) throw new AssertionError("three notices expected");
        if (!notices.get(2).body().equals("Your legal deadline is in 3 days.")) throw new AssertionError("deadline decision missing");
        System.out.println("business decision passed: three-day deadline selects a direct follow-up");
    }
}
