package inspection;
import java.util.Objects;
public record SourceCodeFile(String name, String module, int linesOfCode, int complexity,
                             boolean production, boolean hardcodedCredentials, int todoCount)
        implements ProjectElement {
    public SourceCodeFile {
        Checks.text(name, "name"); Checks.text(module, "module");
        Checks.nonNegative(linesOfCode, "linesOfCode"); Checks.nonNegative(complexity, "complexity");
        Checks.nonNegative(todoCount, "todoCount");
    }
    @Override public void accept(ProjectVisitor visitor) { Objects.requireNonNull(visitor).visit(this); }
}
