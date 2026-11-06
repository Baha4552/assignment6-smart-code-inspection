package inspection;
public final class Main {
    private Main() { }
    public static void main(String[] args) {
        Directory project = DemoProjects.fintech();
        SecurityAuditVisitor security = new SecurityAuditVisitor();
        ProjectStatisticsVisitor statistics = new ProjectStatisticsVisitor();
        QualityGateVisitor quality = new QualityGateVisitor();
        ComplianceVisitor compliance = new ComplianceVisitor();
        project.accept(security); project.accept(statistics); project.accept(quality); project.accept(compliance);
        System.out.println("=== fintech-project ===");
        System.out.println(security.report()); System.out.println(statistics.report());
        System.out.println(quality.report()); System.out.println(compliance.report());
        QualityGateVisitor ready = new QualityGateVisitor();
        DemoProjects.releaseReady().accept(ready);
        TechnicalDebtVisitor debt = new TechnicalDebtVisitor();
        project.accept(debt); System.out.println(debt.report());
        System.out.println("=== release-ready ==="); System.out.println(ready.report());
    }
}
