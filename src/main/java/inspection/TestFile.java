package inspection;
import java.util.Objects;
import java.util.Set;
public record TestFile(String name, int linesOfCode, int testCount, int disabledTests,
                       Set<String> testedModules, boolean hardcodedCredentials) implements ProjectElement {
    public TestFile {
        Checks.text(name, "name"); Checks.nonNegative(linesOfCode, "linesOfCode");
        Checks.nonNegative(testCount, "testCount"); Checks.nonNegative(disabledTests, "disabledTests");
        if (disabledTests > testCount) throw new IllegalArgumentException("disabledTests exceeds testCount");
        testedModules = Set.copyOf(testedModules);
        testedModules.forEach(m -> Checks.text(m, "tested module"));
    }
    @Override public void accept(ProjectVisitor visitor) { Objects.requireNonNull(visitor).visit(this); }
}
