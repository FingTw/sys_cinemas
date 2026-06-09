package com.example.cinema.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LogMasker {

    // Regex for phone numbers: matches 10 digits starting with 0
    private static final Pattern PHONE_PATTERN = Pattern.compile("\\b(0\\d{2})(\\d{4})(\\d{2,3})\\b");
    
    // Regex for fields like password, token, email, card in JSON/Form
    private static final Pattern JSON_PASSWORD_PATTERN = Pattern.compile("(?i)\"(password|passwordPlain|token|secret|cvv|cardNumber)\"\\s*:\\s*\"([^\"]+)\"");

    public static String mask(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        // Mask phone numbers: convert e.g., 0912345678 to 09****78 or 09****18
        Matcher phoneMatcher = PHONE_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (phoneMatcher.find()) {
            String firstPart = phoneMatcher.group(1).substring(0, 2); // e.g., "09"
            String lastPart = phoneMatcher.group(3); // e.g., "18"
            phoneMatcher.appendReplacement(sb, firstPart + "****" + lastPart);
        }
        phoneMatcher.appendTail(sb);
        String result = sb.toString();

        // Mask passwords/tokens in JSON structures
        Matcher passwordMatcher = JSON_PASSWORD_PATTERN.matcher(result);
        sb = new StringBuffer();
        while (passwordMatcher.find()) {
            String field = passwordMatcher.group(1);
            passwordMatcher.appendReplacement(sb, "\"" + field + "\":\"[MASKED]\"");
        }
        passwordMatcher.appendTail(sb);

        return sb.toString();
    }
}
