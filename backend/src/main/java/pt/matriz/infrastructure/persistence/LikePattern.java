package pt.matriz.infrastructure.persistence;

/** Builds case-insensitive LIKE patterns; escapes \ % _ so user input is matched literally. */
final class LikePattern {

    private LikePattern() {
    }

    /** Use with {@code lower(column) like :pattern escape '\'}. */
    static String contains(String text) {
        String escaped = text.strip().toLowerCase()
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        return "%" + escaped + "%";
    }
}
