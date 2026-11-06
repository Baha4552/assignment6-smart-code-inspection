package inspection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
public final class ComplianceVisitor extends TraversingVisitor {
    public record Violation(String element, String rule, String message) { }
    private final Set<String> prohibited;
    private final Set<String> testedModules = new HashSet<>();
    private final List<SourceCodeFile> production = new ArrayList<>();
    private final List<Violation> violations = new ArrayList<>();
    public ComplianceVisitor() { this(Set.of("legacy-crypto")); }
    public ComplianceVisitor(Set<String> prohibited) { this.prohibited = Set.copyOf(prohibited); }
    private void credentials(String name, boolean exposed) {
        if (exposed) violations.add(new Violation(name, "COMP001", "Exposed credentials"));
    }
    @Override public void visit(SourceCodeFile f) {
        credentials(f.name(), f.hardcodedCredentials());
        if (f.production()) production.add(f);
    }
    @Override public void visit(TestFile f) {
        credentials(f.name(), f.hardcodedCredentials());
        if (f.testCount() > f.disabledTests()) testedModules.addAll(f.testedModules());
    }
    @Override public void visit(ConfigurationFile f) { credentials(f.name(), f.hardcodedCredentials()); }
    @Override public void visit(DependencyFile f) {
        for (Dependency d : f.dependencies()) {
            if (d.version().isBlank()) violations.add(new Violation(f.name(), "COMP002", "Missing version: " + d.name()));
            if (prohibited.contains(d.name())) violations.add(new Violation(f.name(), "COMP004", "Prohibited dependency: " + d.name()));
        }
    }
    public List<Violation> violations() {
        List<Violation> result = new ArrayList<>(violations);
        for (SourceCodeFile f : production) if (!testedModules.contains(f.module()))
            result.add(new Violation(f.name(), "COMP003", "Production module has no active tests: " + f.module()));
        return List.copyOf(result);
    }
    public boolean compliant() { return violations().isEmpty(); }
    public String report() {
        StringBuilder out = new StringBuilder("COMPLIANCE\nResult: " + (compliant() ? "PASS" : "FAIL") + "\n");
        for (Violation v : violations()) out.append(v.rule()).append(" ").append(v.element()).append(": ").append(v.message()).append('\n');
        return out.toString();
    }
}
