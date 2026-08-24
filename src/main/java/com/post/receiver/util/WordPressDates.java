package com.post.receiver.util;

public final class WordPressDates {

    private WordPressDates() {
    }

    public static String toIsoLocal(String wordpressDate) {
        if (wordpressDate == null || wordpressDate.isBlank()) {
            return null;
        }
        return wordpressDate.trim().replace(' ', 'T');
    }
}
