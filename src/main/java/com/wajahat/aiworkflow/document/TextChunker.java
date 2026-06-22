package com.wajahat.aiworkflow.document;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class TextChunker {

    private static final int MAX_CHARS = 1200;

    public List<String> chunk(String text) {
        String normalized = text == null ? "" : text.trim();

        List<String> chunks = new ArrayList<>();
        if (normalized.isBlank()) {
            return chunks;
        }

        int start = 0;

        while (start < normalized.length()) {
            int end = Math.min(start + MAX_CHARS, normalized.length());

            if (end < normalized.length()) {
                int lastBreak = Math.max(
                        normalized.lastIndexOf("\n", end),
                        normalized.lastIndexOf(". ", end)
                );

                if (lastBreak > start + 300) {
                    end = lastBreak + 1;
                }
            }

            chunks.add(normalized.substring(start, end).trim());
            start = end;
        }

        return chunks;
    }

    public int estimateTokens(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        return Math.max(1, text.length() / 4);
    }
}