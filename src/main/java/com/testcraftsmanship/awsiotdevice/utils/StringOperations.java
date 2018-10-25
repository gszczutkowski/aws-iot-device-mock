package com.testcraftsmanship.awsiotdevice.utils;

import java.util.Random;

public final class StringOperations {
    private static final String DIGITS = "1234567890";
    private static final String LOWERCASE_LETTERS = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPERCASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final int NUMBER_OF_CHARS_IN_AWS_CLIENT_ID = 26;

    private StringOperations() {
    }

    public static String generateAwsClientId() {
        return generate(DIGITS + LOWERCASE_LETTERS + UPERCASE_LETTERS, NUMBER_OF_CHARS_IN_AWS_CLIENT_ID);
    }

    public static String minimize(String text) {
        return text.replaceAll("\\s+", "");
    }

    private static String generate(CharSequence charClass, int max) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < max; i++) {
            sb.append(charClass.charAt(random.nextInt(charClass.length())));
        }
        return sb.toString();
    }
}
