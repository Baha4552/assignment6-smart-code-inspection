package inspection;
import java.util.Objects;
public record Dependency(String name, String version, boolean vulnerable) {
    public Dependency { Checks.text(name, "name"); Objects.requireNonNull(version, "version"); }
}
