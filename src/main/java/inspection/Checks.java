package inspection;
import java.util.Objects;
final class Checks {
    private Checks() { }
    static String text(String value, String field) {
        Objects.requireNonNull(value, field);
        if (value.isBlank()) throw new IllegalArgumentException(field + " must not be blank");
        return value;
    }
    static int nonNegative(int value, String field) {
        if (value < 0) throw new IllegalArgumentException(field + " must be nonnegative");
        return value;
    }
}
