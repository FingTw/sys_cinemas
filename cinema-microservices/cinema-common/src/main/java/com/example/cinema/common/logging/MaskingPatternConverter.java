package com.example.cinema.common.logging;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Custom Log4j2 Pattern Converter to mask sensitive information like Phone Numbers, Emails, and Passwords.
 * Usage: Replace %msg with %maskMsg in log4j2.xml
 */
@Plugin(name = "MaskingPatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({"maskMsg"})
public class MaskingPatternConverter extends LogEventPatternConverter {

    // Regex for Phone Number (10 digits starting with 0) -> 09******18
    private static final Pattern PHONE_PATTERN = Pattern.compile("(?<!\\d)(0[1-9])\\d{6}(\\d{2})(?!\\d)");
    
    // Regex for Email -> a***@gmail.com
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(?i)([a-zA-Z0-9])([a-zA-Z0-9_.-]*)@([a-zA-Z0-9.-]+)");
    
    // Regex for Password/Secret -> password=******
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(password|pwd|pass|secret)[\"\\s=:]+([^\",\\s;]+)");

    protected MaskingPatternConverter(String name, String style) {
        super(name, style);
    }

    public static MaskingPatternConverter newInstance(final String[] options) {
        return new MaskingPatternConverter("maskMsg", "maskMsg");
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        String message = event.getMessage().getFormattedMessage();
        if (message != null && !message.isEmpty()) {
            message = mask(message);
        }
        toAppendTo.append(message);
    }

    private String mask(String message) {
        // Mask Phone
        Matcher phoneMatcher = PHONE_PATTERN.matcher(message);
        message = phoneMatcher.replaceAll("$1******$2");

        // Mask Email
        Matcher emailMatcher = EMAIL_PATTERN.matcher(message);
        message = emailMatcher.replaceAll("$1***@$3");

        // Mask Password
        Matcher passwordMatcher = PASSWORD_PATTERN.matcher(message);
        message = passwordMatcher.replaceAll("$1=******");

        return message;
    }
}
