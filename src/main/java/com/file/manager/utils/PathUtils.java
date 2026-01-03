package com.file.manager.utils;

import org.springframework.stereotype.Component;

@Component
public final class PathUtils {
    private PathUtils() {}

    // Convert a name to a safe path segment: " My  Folder  (2025) " -> "my-folder-2025"
    public static String toSegment(String raw) {
        if (raw == null) return "";
        String s = raw.trim();

        // Normalize Unicode accents: "Café" -> "Cafe"
        s = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "");

        // Replace any non-alphanumeric char with a hyphen
        s = s.replaceAll("[^A-Za-z0-9]+", "-");

        // Collapse multiple hyphens
        s = s.replaceAll("-{2,}", "-");

        // Trim hyphens from ends
        s = s.replaceAll("(^-+|-+$)", "");

        // Lower-case for consistency
        return s.toLowerCase();
    }

    // Join parent path with a segment, ensuring single slashes
    public static String join(String parentPath, String segment) {
        String p = parentPath == null || parentPath.isBlank() ? "/" : parentPath.trim();
        if (!p.startsWith("/")) p = "/" + p;
        if (p.length() > 1 && p.endsWith("/")) p = p.substring(0, p.length() - 1);

        String seg = toSegment(segment);
        if (seg.isBlank()) return p; // fallback: do not change path if name sanitizes to empty
        return p.equals("/") ? "/" + seg : p + "/" + seg;
    }
}
