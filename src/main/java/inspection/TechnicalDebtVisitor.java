package inspection;
public final class TechnicalDebtVisitor extends TraversingVisitor {
    private long minutes;
    @Override public void visit(SourceCodeFile f) {
        minutes += Math.max(0L, (long) f.complexity() - 10) * 30L + (long) f.todoCount() * 15L;
    }
    @Override public void visit(TestFile f) { minutes += (long) f.disabledTests() * 10L; }
    @Override public void visit(ConfigurationFile f) { if (f.insecureConfiguration()) minutes += 60L; }
    @Override public void visit(DependencyFile f) {
        for (Dependency d : f.dependencies()) {
            if (d.vulnerable()) minutes += 120L;
            if (d.version().isBlank()) minutes += 30L;
        }
    }
    public long minutes() { return minutes; }
    public String report() { return "TECHNICAL DEBT\nEstimated remediation: " + minutes + " minutes\n"; }
}
