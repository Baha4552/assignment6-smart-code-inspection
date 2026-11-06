package inspection;
import java.util.List;
import java.util.Objects;
public record DependencyFile(String name, int linesOfCode, List<Dependency> dependencies) implements ProjectElement {
    public DependencyFile {
        Checks.text(name, "name"); Checks.nonNegative(linesOfCode, "linesOfCode");
        dependencies = List.copyOf(dependencies);
    }
    @Override public void accept(ProjectVisitor visitor) { Objects.requireNonNull(visitor).visit(this); }
}
