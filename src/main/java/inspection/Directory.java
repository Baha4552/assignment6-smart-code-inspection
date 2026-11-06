package inspection;
import java.util.List;
import java.util.Objects;
public record Directory(String name, List<ProjectElement> children) implements ProjectElement {
    public Directory { Checks.text(name, "name"); children = List.copyOf(children); }
    @Override public void accept(ProjectVisitor visitor) { Objects.requireNonNull(visitor).visit(this); }
}
