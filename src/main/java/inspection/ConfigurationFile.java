package inspection;
import java.util.Objects;
public record ConfigurationFile(String name, int linesOfCode, boolean hardcodedCredentials,
                                boolean insecureConfiguration) implements ProjectElement {
    public ConfigurationFile { Checks.text(name, "name"); Checks.nonNegative(linesOfCode, "linesOfCode"); }
    @Override public void accept(ProjectVisitor visitor) { Objects.requireNonNull(visitor).visit(this); }
}
