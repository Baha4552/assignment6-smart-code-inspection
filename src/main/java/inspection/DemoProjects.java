package inspection;
import java.util.List;
import java.util.Set;
public final class DemoProjects {
    private DemoProjects() { }
    public static Directory fintech() {
        return new Directory("fintech-project", List.of(
            new Directory("src", List.of(
                new SourceCodeFile("PaymentService.java", "payments.PaymentService", 240, 18, true, true, 3),
                new SourceCodeFile("UserService.java", "users.UserService", 150, 8, true, false, 1))),
            new Directory("tests", List.of(new TestFile("PaymentServiceTest.java", 120, 8, 1,
                Set.of("payments.PaymentService"), false))),
            new Directory("config", List.of(new ConfigurationFile("application.properties", 24, true, true))),
            new DependencyFile("pom.xml", 46, List.of(
                new Dependency("payments-sdk", "2.1.0", false),
                new Dependency("legacy-crypto", "1.0.0", true),
                new Dependency("logging-api", "", false)))));
    }
    public static Directory releaseReady() {
        return new Directory("release-ready", List.of(
            new SourceCodeFile("PaymentService.java", "payments.PaymentService", 100, 8, true, false, 0),
            new TestFile("PaymentServiceTest.java", 80, 6, 0, Set.of("payments.PaymentService"), false),
            new ConfigurationFile("application.properties", 10, false, false),
            new DependencyFile("pom.xml", 30, List.of(new Dependency("payments-sdk", "2.1.0", false)))));
    }
}
