package inspection;
import java.util.ArrayList;
import java.util.List;
public final class SecurityAuditVisitor extends TraversingVisitor {
    public enum Severity { CRITICAL, HIGH }
    public record Issue(Severity severity, String element, String rule, String message) { }
    private final List<Issue> issues = new ArrayList<>();
    private void credentials(String name, boolean exposed) {
        if (exposed) issues.add(new Issue(Severity.CRITICAL, name, "SEC001", "Hardcoded credentials"));
    }
    @Override public void visit(SourceCodeFile file) {
        credentials(file.name(), file.hardcodedCredentials());
        if (file.complexity() > 15)
            issues.add(new Issue(Severity.HIGH, file.name(), "SEC004", "High-risk source complexity > 15"));
    }
    @Override public void visit(TestFile file) { credentials(file.name(), file.hardcodedCredentials()); }
    @Override public void visit(ConfigurationFile file) {
        credentials(file.name(), file.hardcodedCredentials());
        if (file.insecureConfiguration())
            issues.add(new Issue(Severity.CRITICAL, file.name(), "SEC003", "Insecure configuration"));
    }
    @Override public void visit(DependencyFile file) {
        for (Dependency dep : file.dependencies()) if (dep.vulnerable())
            issues.add(new Issue(Severity.CRITICAL, file.name(), "SEC002", "Vulnerable dependency: " + dep.name()));
    }
    public List<Issue> issues() { return List.copyOf(issues); }
    public long criticalIssues() { return issues.stream().filter(i -> i.severity() == Severity.CRITICAL).count(); }
    public int score() {
        long high = issues.size() - criticalIssues();
        return (int) Math.max(0L, 100L - 25L * criticalIssues() - 10L * high);
    }
    public String report() {
        StringBuilder out = new StringBuilder("SECURITY AUDIT\nScore: " + score() + "/100\nCritical issues: " + criticalIssues() + "\n");
        for (Issue issue : issues) out.append(issue.severity()).append(" ").append(issue.rule())
            .append(" ").append(issue.element()).append(": ").append(issue.message()).append('\n');
        return out.toString();
    }
}
