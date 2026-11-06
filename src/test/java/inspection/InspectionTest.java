package inspection;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
class InspectionTest {
    private SourceCodeFile source(int complexity, boolean secret) {
        return new SourceCodeFile("A.java", "app.A", 100, complexity, true, secret, 0);
    }
    private TestFile tests(int total, int disabled) {
        return new TestFile("ATest.java", 40, total, disabled, Set.of("app.A"), false);
    }
    @Test void dispatchesEveryElementToItsExactOverload() {
        List<String> calls = new ArrayList<>();
        ProjectVisitor v = new ProjectVisitor() {
            public void visit(SourceCodeFile f) { calls.add("source"); }
            public void visit(TestFile f) { calls.add("test"); }
            public void visit(ConfigurationFile f) { calls.add("config"); }
            public void visit(DependencyFile f) { calls.add("dependency"); }
            public void visit(Directory d) { calls.add("directory"); }
        };
        List<ProjectElement> elements = List.of(source(1,false), tests(1,0),
            new ConfigurationFile("c",1,false,false), new DependencyFile("p",1,List.of()), new Directory("d",List.of()));
        elements.forEach(e -> e.accept(v));
        assertEquals(List.of("source","test","config","dependency","directory"), calls);
    }
    @Test void securityDetectsSourceCredentialsAndHighRisk() {
        SecurityAuditVisitor v = new SecurityAuditVisitor(); source(16,true).accept(v);
        assertEquals(2,v.issues().size()); assertEquals(1,v.criticalIssues()); assertEquals(65,v.score());
    }
    @Test void securityDetectsTestCredentials() {
        SecurityAuditVisitor v = new SecurityAuditVisitor();
        new TestFile("t",1,1,0,Set.of(),true).accept(v); assertEquals(1,v.criticalIssues());
    }
    @Test void securityDetectsInsecureConfigurationAndSecretIndependently() {
        SecurityAuditVisitor v = new SecurityAuditVisitor();
        new ConfigurationFile("c",1,true,true).accept(v); assertEquals(2,v.criticalIssues()); assertEquals(50,v.score());
    }
    @Test void securityDetectsOnlyFlaggedDependencies() {
        SecurityAuditVisitor v = new SecurityAuditVisitor();
        new DependencyFile("pom",1,List.of(new Dependency("good","1",false),new Dependency("bad","1",true))).accept(v);
        assertEquals(1,v.criticalIssues()); assertEquals("SEC002",v.issues().get(0).rule());
    }
    @Test void cleanSecurityReportHasPerfectScore() {
        SecurityAuditVisitor v = new SecurityAuditVisitor(); DemoProjects.releaseReady().accept(v);
        assertTrue(v.issues().isEmpty()); assertEquals(100,v.score());
    }
    @Test void securityScoreNeverDropsBelowZero() {
        SecurityAuditVisitor v = new SecurityAuditVisitor();
        for(int i=0;i<10;i++) source(20,true).accept(v); assertEquals(0,v.score());
    }
    @Test void realisticStatisticsIncludeEveryFileKind() {
        ProjectStatisticsVisitor v = new ProjectStatisticsVisitor(); DemoProjects.fintech().accept(v);
        assertAll(() -> assertEquals(5,v.totalFiles()), () -> assertEquals(580,v.linesOfCode()),
            () -> assertEquals(2,v.sourceFiles()), () -> assertEquals(1,v.testFiles()),
            () -> assertEquals(8,v.tests()), () -> assertEquals(3,v.dependencies()),
            () -> assertEquals(13.0,v.averageComplexity()), () -> assertEquals(0.5,v.testSourceRatio()));
    }
    @Test void nestedDirectoriesAreVisitedExactlyOnce() {
        Directory root = new Directory("root",List.of(new Directory("a",List.of(new Directory("b",List.of(source(4,false)))))));
        ProjectStatisticsVisitor v = new ProjectStatisticsVisitor(); root.accept(v);
        assertEquals(1,v.totalFiles()); assertEquals(100,v.linesOfCode()); assertEquals(4,v.averageComplexity());
    }
    @Test void handlesTwoHundredNestedDirectories() {
        ProjectElement root = source(2,false);
        for(int i=0;i<200;i++) root = new Directory("d"+i,List.of(root));
        ProjectStatisticsVisitor v = new ProjectStatisticsVisitor(); root.accept(v); assertEquals(1,v.totalFiles());
    }
    @Test void emptyDirectoryHasFiniteZeroStatisticsAndFailsGate() {
        Directory root = new Directory("empty",List.of());
        ProjectStatisticsVisitor s = new ProjectStatisticsVisitor(); root.accept(s);
        QualityGateVisitor q = new QualityGateVisitor(); root.accept(q);
        assertEquals(0,s.totalFiles()); assertEquals(0.0,s.averageComplexity()); assertEquals(0.0,s.testSourceRatio());
        assertFalse(q.passed()); assertFalse(q.scopePassed());
    }
    @Test void testOnlyProjectDoesNotDivideByZero() {
        ProjectStatisticsVisitor v = new ProjectStatisticsVisitor(); tests(3,0).accept(v);
        assertEquals(0.0,v.testSourceRatio()); assertEquals(3,v.tests());
    }
    @Test void qualityGatePassesForReleaseReadyProject() {
        QualityGateVisitor q = new QualityGateVisitor(); DemoProjects.releaseReady().accept(q);
        assertTrue(q.passed()); assertTrue(q.report().contains("FINAL RESULT: PASSED"));
    }
    @Test void qualityGateFailsSecurityIndependently() {
        QualityGateVisitor q = new QualityGateVisitor();
        new Directory("p",List.of(source(1,true),tests(1,0))).accept(q);
        assertFalse(q.securityPassed()); assertTrue(q.complexityPassed()); assertTrue(q.testsPassed()); assertFalse(q.passed());
    }
    @Test void qualityGateFailsComplexityIndependently() {
        QualityGateVisitor q = new QualityGateVisitor();
        new Directory("p",List.of(source(11,false),tests(1,0))).accept(q);
        assertTrue(q.securityPassed()); assertFalse(q.complexityPassed()); assertTrue(q.testsPassed()); assertFalse(q.passed());
    }
    @Test void qualityGateFailsMissingTestsIndependently() {
        QualityGateVisitor q = new QualityGateVisitor(); source(1,false).accept(q);
        assertTrue(q.securityPassed()); assertTrue(q.complexityPassed()); assertFalse(q.testsPassed()); assertFalse(q.passed());
    }
    @Test void exactGateThresholdsAreInclusive() {
        List<ProjectElement> items = new ArrayList<>();
        for(int i=0;i<5;i++) items.add(new SourceCodeFile("A"+i,"m"+i,1,10,true,false,0));
        items.add(tests(1,0)); items.add(tests(1,0));
        QualityGateVisitor q = new QualityGateVisitor(); new Directory("p",items).accept(q); assertTrue(q.passed());
    }
    @Test void complianceDetectsEveryRequiredViolation() {
        ComplianceVisitor c = new ComplianceVisitor(); DemoProjects.fintech().accept(c);
        assertFalse(c.compliant()); assertEquals(5,c.violations().size());
        assertEquals(Set.of("COMP001","COMP002","COMP003","COMP004"),
            c.violations().stream().map(ComplianceVisitor.Violation::rule).collect(java.util.stream.Collectors.toSet()));
    }
    @Test void complianceIsIndependentOfTestTraversalOrder() {
        ComplianceVisitor first = new ComplianceVisitor(), last = new ComplianceVisitor();
        new Directory("p",List.of(tests(1,0),source(1,false))).accept(first);
        new Directory("p",List.of(source(1,false),tests(1,0))).accept(last);
        assertTrue(first.compliant()); assertTrue(last.compliant()); assertEquals(first.violations(),last.violations());
    }
    @Test void disabledOrZeroTestsDoNotSatisfyProductionCoverage() {
        for(TestFile t : List.of(tests(0,0),tests(2,2))) {
            ComplianceVisitor c = new ComplianceVisitor(); new Directory("p",List.of(source(1,false),t)).accept(c);
            assertEquals("COMP003",c.violations().get(0).rule());
        }
    }
    @Test void nonProductionModuleDoesNotRequireTests() {
        ComplianceVisitor c = new ComplianceVisitor();
        new SourceCodeFile("Tool.java","tools",1,1,false,false,0).accept(c); assertTrue(c.compliant());
    }
    @Test void complianceReportsAreIdempotentAndCleanProjectPasses() {
        ComplianceVisitor c = new ComplianceVisitor(); DemoProjects.releaseReady().accept(c);
        assertTrue(c.compliant()); assertEquals(c.report(),c.report()); assertTrue(c.violations().isEmpty());
    }
    @Test void configurableProhibitedDependenciesAndWhitespaceVersion() {
        ComplianceVisitor c = new ComplianceVisitor(Set.of("banned"));
        new DependencyFile("pom",1,List.of(new Dependency("banned","  ",false))).accept(c);
        assertEquals(2,c.violations().size());
    }
    @Test void invalidInputFailsEarly() {
        assertThrows(IllegalArgumentException.class,() -> source(-1,false));
        assertThrows(IllegalArgumentException.class,() -> new ConfigurationFile(" ",1,false,false));
        assertThrows(IllegalArgumentException.class,() -> tests(1,2));
        assertThrows(NullPointerException.class,() -> new Directory("x",null));
        assertThrows(NullPointerException.class,() -> source(1,false).accept(null));
    }
    @Test void directoryAndDependencyListsAreDefensivelyCopied() {
        List<ProjectElement> children = new ArrayList<>(); children.add(source(1,false));
        Directory d = new Directory("d",children); children.clear(); assertEquals(1,d.children().size());
        assertThrows(UnsupportedOperationException.class,() -> d.children().clear());
        List<Dependency> deps = new ArrayList<>(); deps.add(new Dependency("x","1",false));
        DependencyFile f = new DependencyFile("pom",1,deps); deps.clear(); assertEquals(1,f.dependencies().size());
    }
    @Test void returnedFindingsCannotBeMutated() {
        SecurityAuditVisitor s = new SecurityAuditVisitor(); source(1,true).accept(s);
        assertThrows(UnsupportedOperationException.class,() -> s.issues().clear());
        ComplianceVisitor c = new ComplianceVisitor(); source(1,false).accept(c);
        assertThrows(UnsupportedOperationException.class,() -> c.violations().clear());
    }
    @Test void complexitySecurityBoundaryFifteenIsNotHighRisk() {
        SecurityAuditVisitor s = new SecurityAuditVisitor(); source(15,false).accept(s); assertEquals(0,s.issues().size());
    }
}
