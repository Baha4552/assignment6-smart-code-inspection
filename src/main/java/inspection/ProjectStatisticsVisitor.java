package inspection;
import java.util.Locale;
public final class ProjectStatisticsVisitor extends TraversingVisitor {
    private long totalFiles, linesOfCode, sourceFiles, testFiles, tests, dependencies, complexity;
    private void file(int lines) { totalFiles++; linesOfCode += lines; }
    @Override public void visit(SourceCodeFile f) { file(f.linesOfCode()); sourceFiles++; complexity += f.complexity(); }
    @Override public void visit(TestFile f) { file(f.linesOfCode()); testFiles++; tests += f.testCount(); }
    @Override public void visit(ConfigurationFile f) { file(f.linesOfCode()); }
    @Override public void visit(DependencyFile f) { file(f.linesOfCode()); dependencies += f.dependencies().size(); }
    public long totalFiles() { return totalFiles; }
    public long linesOfCode() { return linesOfCode; }
    public long sourceFiles() { return sourceFiles; }
    public long testFiles() { return testFiles; }
    public long tests() { return tests; }
    public long dependencies() { return dependencies; }
    public double averageComplexity() { return sourceFiles == 0 ? 0.0 : (double) complexity / sourceFiles; }
    public double testSourceRatio() { return sourceFiles == 0 ? 0.0 : (double) testFiles / sourceFiles; }
    public String report() {
        return String.format(Locale.ROOT, "PROJECT STATISTICS%nTotal files: %d%nLines of code: %d%nSource files: %d%nTest files: %d%nTests: %d%nDependencies: %d%nAverage complexity: %.2f%nTest/source ratio: %.2f%n",
            totalFiles, linesOfCode, sourceFiles, testFiles, tests, dependencies, averageComplexity(), testSourceRatio());
    }
}
