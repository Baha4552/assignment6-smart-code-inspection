# Assignment requirement checklist

| Requirement | Implementation or evidence |
| --- | --- |
| Java 17 or newer | `pom.xml` release 17 and Java 17/21 CI matrix |
| Five concrete element types | SourceCodeFile, TestFile, ConfigurationFile, DependencyFile, Directory |
| Meaningful metadata | Validated record components including complexity, LOC, test targets, flags and dependency versions |
| Nested directories | Directory children are ProjectElement values; recursive TraversingVisitor |
| Exact visitor interface | ProjectVisitor has all five required visit overloads |
| accept and double dispatch | Every element calls `visitor.visit(this)`; exact-overload test and README explanation |
| No instanceof or type-switch routing | Typed visit methods throughout production code |
| Security audit | Four documented rules, typed findings, bounded score |
| Statistics | All six requested metrics plus source/test file counts |
| Quality gate | Independent security, complexity, ratio checks and combined PASS/FAIL |
| Compliance | All four requested business rule categories |
| Additional Visitor | TechnicalDebtVisitor in a separate commit; existing elements untouched |
| New element limitation | DatabaseMigrationFile change list in README |
| Realistic fintech structure | DemoProjects.fintech with the exact requested names and hierarchy |
| Same tree for all operations | Main passes the same `project` reference to all five visitors |
| At least 12 JUnit 5 tests | 31 behavioral tests including boundary, empty, nested, PASS/FAIL and extra visitor |
| Class UML | docs/UML.md class diagram with five elements, interface and five concrete visitors |
| Sequence UML | docs/UML.md sequence diagram with nested traversal and typed dispatch |
| Eight README design answers | Dedicated design questions section, plus extension limitation |
| At least eight meaningful commits | Incremental local history retained in .git and project.bundle |
| README or PDF | README.md is the submission document |
| Public GitHub repository | Public repository URL supplied separately after successful publication |
| Moodle submission | Upload README.md and provide the public GitHub URL in the student's account |
| Individual defense and live challenge | docs/DEFENSE_RU.md preparation; student performs the actual defense |

See `docs/VERIFICATION.md` for the actual local build results and `docs/demo-output.txt` for the captured reports. Remote CI and Moodle completion must be checked separately; local tests do not imply those actions have happened.
