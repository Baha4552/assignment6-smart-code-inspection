package inspection;
public final class QualityGateVisitor extends TraversingVisitor {
    private final ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
    private final SecurityAuditVisitor security = new SecurityAuditVisitor();
    private void collect(ProjectElement file) { file.accept(statistics); file.accept(security); }
    @Override public void visit(SourceCodeFile f) { collect(f); }
    @Override public void visit(TestFile f) { collect(f); }
    @Override public void visit(ConfigurationFile f) { collect(f); }
    @Override public void visit(DependencyFile f) { collect(f); }
    public boolean securityPassed() { return security.criticalIssues() == 0; }
    public boolean complexityPassed() { return statistics.averageComplexity() <= 10.0; }
    public boolean testsPassed() { return statistics.testSourceRatio() >= 0.40; }
    public boolean scopePassed() { return statistics.sourceFiles() > 0; }
    public boolean passed() { return scopePassed() && securityPassed() && complexityPassed() && testsPassed(); }
    private String status(boolean result) { return result ? "PASS" : "FAIL"; }
    public String report() {
        return "QUALITY GATE\nSecurity: " + status(securityPassed()) + "\nComplexity: " + status(complexityPassed())
            + "\nTests: " + status(testsPassed()) + "\nProduction scope: " + status(scopePassed())
            + "\nFINAL RESULT: " + (passed() ? "PASSED" : "FAILED") + "\n";
    }
}
