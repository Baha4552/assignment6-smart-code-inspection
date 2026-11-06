package inspection;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class TechnicalDebtTest {
    @Test void fintechDebtIncludesAllFourKindsOfFile() {
        TechnicalDebtVisitor v = new TechnicalDebtVisitor(); DemoProjects.fintech().accept(v);
        assertEquals(520,v.minutes()); assertTrue(v.report().contains("520 minutes"));
    }
    @Test void cleanProjectAndEmptyDirectoryHaveZeroDebt() {
        TechnicalDebtVisitor v = new TechnicalDebtVisitor(); DemoProjects.releaseReady().accept(v);
        new Directory("empty",List.of()).accept(v); assertEquals(0,v.minutes());
    }
    @Test void nestedTraversalAndComplexityThreshold() {
        TechnicalDebtVisitor v = new TechnicalDebtVisitor();
        new Directory("root",List.of(new Directory("nested",List.of(
            new SourceCodeFile("x","x",1,10,true,false,2))))).accept(v); assertEquals(30,v.minutes());
    }
    @Test void largeValuesUseLongArithmetic() {
        TechnicalDebtVisitor v = new TechnicalDebtVisitor();
        new SourceCodeFile("x","x",0,Integer.MAX_VALUE,false,false,Integer.MAX_VALUE).accept(v);
        assertEquals(96_636_763_815L,v.minutes());
    }
}
