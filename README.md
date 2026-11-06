# Smart Code Inspection and Compliance Engine

Assignment 6 implements the Visitor pattern in Java 17. Five immutable project element types are analyzed by five independent operations: security, statistics, release quality, compliance and technical debt. The fintech example intentionally fails release checks; a second example passes.

## Build and run

Install a **JDK 17 or newer** and **Maven 3.9 or newer**. Ensure `java` and `mvn` are on PATH and `JAVA_HOME` points to the JDK. The first build needs internet access to Maven Central.

```sh
mvn clean verify
java -jar target/smart-code-inspection-1.0.0.jar
```

No application dependencies are needed at runtime. JUnit 5.11.4 is test-only. The compiler uses `--release 17`; the executable JAR contains `inspection.Main`. GitHub Actions builds and tests on Java 17 and 21.

## Project layout

```text
src/main/java/inspection/
  ProjectElement.java          Element interface
  ProjectVisitor.java          Five visit overloads
  SourceCodeFile.java          Production metadata and complexity
  TestFile.java                Test counts and covered module identifiers
  ConfigurationFile.java       Configuration security metadata
  DependencyFile.java          Dependency declarations
  Directory.java               Immutable collection of ProjectElement
  Dependency.java              A dependency value object, not an element
  TraversingVisitor.java       Shared recursive traversal and leaf defaults
  SecurityAuditVisitor.java    Security findings and score
  ProjectStatisticsVisitor.java
  QualityGateVisitor.java
  ComplianceVisitor.java
  TechnicalDebtVisitor.java    Additional operation in its own commit
  DemoProjects.java            Two in-memory project fixtures
  Main.java                    Runs reports
src/test/java/inspection/       JUnit behavioral tests
docs/UML.md                    Class and sequence diagrams
docs/DEFENSE_RU.md             Defense notes and modification exercises
docs/REQUIREMENTS.md           Requirement-to-implementation checklist
docs/demo-output.txt           Captured execution output
```

## Fintech demonstration

`DemoProjects.fintech()` creates this element tree:

```text
fintech-project/
├── src/
│   ├── PaymentService.java
│   └── UserService.java
├── tests/
│   └── PaymentServiceTest.java
├── config/
│   └── application.properties
└── pom.xml
```

This is an **in-memory metadata inspection engine**, not a filesystem parser, source-code scanner or live vulnerability database. File names, line counts, complexity and security flags are explicit demo inputs. `Dependency.vulnerable` represents an upstream scan result; no CVE claims are made about real packages. No real credentials are included. The fixture `pom.xml` represents the inspected fintech application's dependencies, not the engine's build file.

All five visitors receive the **same tree instance** in `Main`. The example has two source files (240 and 150 lines; complexities 18 and 8), one 120-line test file with eight declared tests (one disabled), a 24-line configuration file and a 46-line dependency file containing three dependency declarations.

Expected statistics: **5 files, 580 lines, 8 tests, 3 dependencies, average complexity 13.00, test/source ratio 0.50**. The security audit finds four critical issues and one high issue, producing a score of **0/100**. Quality reports `Security: FAIL`, `Complexity: FAIL`, `Tests: PASS`, `Production scope: PASS`, and `FINAL RESULT: FAILED`. Compliance finds five violations. Technical debt is **520 minutes**. The `release-ready` fixture passes all quality criteria.

See [captured demo output](docs/demo-output.txt) and [UML diagrams](docs/UML.md).

## Operation rules and metric definitions

### Security audit

| Rule | Trigger | Severity |
| --- | --- | --- |
| SEC001 | Hardcoded credentials in source, test or configuration | CRITICAL |
| SEC002 | A dependency is marked vulnerable | CRITICAL |
| SEC003 | Configuration is marked insecure | CRITICAL |
| SEC004 | Source complexity is greater than 15 | HIGH |

The score is `max(0, 100 - 25 * criticalIssues - 10 * highIssues)`. Multiple independent findings on one file are counted separately. A complexity of exactly 15 does not trigger SEC004.

### Project statistics

* Total files counts source, test, configuration and dependency files; directories are not files.
* Lines of code sums the supplied line count of all four file kinds.
* Number of tests sums declared test cases, **including disabled tests**; disabled tests are tracked separately by the test elements.
* Dependencies counts declarations, not distinct artifact names or transitive dependencies.
* Average complexity is the arithmetic mean of source-file complexity values; test/configuration/dependency files are excluded.
* Test/source ratio is **test file count / source file count**, not test-case count or measured coverage.
* No source files means average complexity and ratio are both `0.0`, avoiding division by zero.

### Quality gate

The project passes only when there is at least one source file, the average source complexity is at most 10, test/source file ratio is at least 0.40, and the number of critical security findings is zero. Threshold comparisons are inclusive. `Production scope` means source files exist; it does not filter by the `production` metadata flag. Empty and test-only projects cannot pass.

`QualityGateVisitor` delegates each leaf to private statistics and security visitors, so the rules are reused. Its inherited directory traversal visits each child once; the delegates never receive directories from this visitor, preventing double counting. The ratio is a coarse assignment metric: it can pass even if a test file has no runnable cases. `ComplianceVisitor` separately requires active declared tests for each production module. The quality gate does not include every compliance rule or high-severity security issue.

### Compliance

| Rule | Trigger |
| --- | --- |
| COMP001 | Exposed credentials in a source, test or configuration file |
| COMP002 | A dependency version is empty or whitespace |
| COMP003 | A production source module has no test file declaring at least one active test for that module |
| COMP004 | A dependency name is in the prohibited set, by default `legacy-crypto` |

`module` and `testedModules` use exact, case-sensitive logical identifiers such as `payments.PaymentService`. They need not match filenames. A test file with at least one active test contributes all its declared module targets; this is declared metadata, not measured code coverage. A set of only disabled tests or zero tests does not satisfy COMP003. Modules with `production == false` are exempt. Findings for missing tests are computed when reporting, after traversal, so source/test directory order does not change the result. Repeated calls to `violations()` or `report()` do not add findings. The prohibited set can be supplied to the constructor. Version validation checks presence only, not reproducibility or semantic version syntax.

### Additional TechnicalDebtVisitor

| Input | Estimated remediation minutes |
| --- | ---: |
| Source complexity above 10 | 30 per excess point |
| Source TODO | 15 each |
| Disabled test | 10 each |
| Insecure configuration | 60 per file |
| Vulnerable dependency | 120 each |
| Missing dependency version | 30 each |

These are explicit illustrative planning weights, not empirical predictions. The fintech sum is `240 + 60 + 10 + 60 + 120 + 30 = 520`. Arithmetic uses `long` to avoid intermediate `int` overflow. The extra visitor, its four tests and demo integration were added in a **separate Git commit**, with no changes to the five element classes or `ProjectVisitor`.

## Visitor design questions

### What problem does Visitor solve in this project

The same heterogeneous tree needs unrelated analyses. Visitor keeps those operations outside the data model while using static overloads instead of runtime type checks. Adding technical debt did not add another analysis method to each file class.

### Where are Element and Visitor

`ProjectElement` defines `accept(ProjectVisitor)`. Its concrete implementations are the five records `SourceCodeFile`, `TestFile`, `ConfigurationFile`, `DependencyFile` and `Directory`. `ProjectVisitor` declares one overload per element type. `TraversingVisitor` implements shared traversal. The five named analysis visitors extend that base class. `Dependency` and the nested finding records are values, not project elements.

### What is double dispatch

For `ProjectElement e = new SourceCodeFile(...)`, the call `e.accept(visitor)` first dynamically selects `SourceCodeFile.accept`. Inside that implementation, `this` has the compile-time type `SourceCodeFile`, so Java selects the signature `visit(SourceCodeFile)`. The runtime visitor object then selects that overload's actual implementation, such as `SecurityAuditVisitor.visit(SourceCodeFile)`. Behavior therefore depends on both the concrete element and the concrete visitor. Java overload selection itself is static; `accept` supplies the correctly typed call site. No `instanceof`, type tags or type-switch dispatch is used.

### Why not put all operations directly inside elements

Methods such as `checkSecurity` and `calculateStatistics` would mix model responsibilities with unrelated rules. Every new report would require edits across the element hierarchy. Elements here validate and hold metadata and dispatch visitors; they do not know scoring, compliance or release policies. Directory traversal belongs to visitors, so `Directory.accept` remains the same one-call dispatch shape as the leaf elements.

### Is adding a Visitor or an Element easier

A new operation is easier: extend `TraversingVisitor`, override the relevant overloads, add tests and invoke it from the client. Existing elements and the visitor interface stay unchanged. A new element changes the closed set of visit signatures and requires reviewing every operation. This tradeoff suits stable element types with frequently added analyses.

### DatabaseMigrationFile limitation challenge

1. Add `DatabaseMigrationFile implements ProjectElement` with metadata such as migration name, SQL lines and destructive-operation flag. Its `accept` calls `visitor.visit(this)`.
2. Add `void visit(DatabaseMigrationFile file)` to `ProjectVisitor`. Without this overload, `DatabaseMigrationFile.accept` cannot compile, because the interface has no compatible visit signature.
3. Add an implementation of that overload to `TraversingVisitor`. All visitors extend this adapter, so an empty default restores compilation for them, but silently ignoring migrations is usually wrong.
4. Review and override the method in security, statistics, quality, compliance and technical debt as appropriate. Statistics must count migration files; quality must forward them to its delegates; security/compliance/debt need explicit migration policies. A direct implementation of `ProjectVisitor`, including the recording visitor in the dispatch test, must also implement the new method to compile.
5. Update fixtures, tests, diagrams and documentation. Existing element records and their `accept` methods do not change. `Directory` already stores any `ProjectElement` and needs no structural change.

The adapter reduces immediate compilation changes to the existing concrete visitors, but it does not remove the need for semantic review of every operation. Keeping the new interface method abstract makes the adapter change mandatory.

### When would Visitor be a bad choice

It is a poor fit when third parties often introduce new element types, when the model has only one simple operation, or when most visitors would ignore most elements. Extending the overload interface becomes costly. Rules requiring data absent from public element metadata may also force unwelcome model changes. This recursive teaching implementation is suitable for ordinary trees; extremely deep untrusted trees should use an explicit stack rather than relying on the Java call stack.

### How is Visitor different from Strategy

Strategy replaces one algorithm behind a common contract, for example choosing a release scoring formula. Visitor defines an operation across several element types and selects a typed overload for each. Here a security visitor understands source, test, configuration, dependency and directory nodes; a strategy need not traverse or use double dispatch. A visitor could itself receive a strategy for scoring.

### Which SOLID principles apply

* **SRP:** elements own validated metadata; security, statistics, compliance and debt rules have separate classes.
* **OCP:** new operations can be added without editing elements. The principle has a deliberate limit: new element types expand the visitor contract.
* **LSP:** every element can be processed through `ProjectElement.accept` while preserving the dispatch contract.
* **ISP:** the five-overload contract is focused on this small stable hierarchy, but all operations share it; many future element types would put pressure on ISP. The adapter supplies deliberate defaults, not a guarantee that every new type is handled meaningfully.
* **DIP:** clients traverse `ProjectElement` with a `ProjectVisitor`. The visitor interface necessarily refers to concrete element types, and the quality gate intentionally composes concrete rule visitors; this is not complete decoupling from all concrete classes.

## Traversal and lifecycle contracts

Create a **fresh visitor for each independent run**. Visitors accumulate state and are not thread-safe. Reusing one across different roots intentionally aggregates results, while visiting the same tree twice doubles counts/findings. Reports return snapshots or derived values and do not perform traversal. The private quality delegates are not exposed.

Names and module identifiers cannot be blank; numeric metadata cannot be negative; disabled tests cannot exceed declared tests. Lists and sets are copied into immutable collections, rejecting null entries. Elements are immutable. Ordinary construction cannot create directory cycles because children must exist before a parent is built. The model permits sharing the same element under two branches; traversal then counts both occurrences, so callers should supply a proper tree for file-based totals. Recursive traversal is tested at depth 200, not claimed to support arbitrary depth.

## Tests and defense

Run `mvn clean verify` to execute **31 meaningful JUnit 5 tests**. They cover exact dispatch for all five element types, every security rule, score limits, all statistic fields, PASS and independent FAIL cases, inclusive thresholds, all compliance rules, traversal order, nested and empty directories, no-source division, immutable inputs/results, input validation and the additional visitor, including large-value arithmetic. There are no `assertNotNull`-only tests.

For individual defense use [Russian notes and live modification exercises](docs/DEFENSE_RU.md). Follow **Predict → Explain → Modify → Run → Test**. The implemented extra visitor is a concrete example of adding an operation without editing elements. The DatabaseMigrationFile explanation above shows the opposite extension tradeoff.

## Git history and submission

The repository contains incremental commits for build setup, model/dispatch, security, statistics, quality gate, compliance, initial demo/tests, the additional visitor, and documentation/verification. Check with:

```sh
git log --oneline --reverse
git show --stat HEAD
git log --oneline -- src/main/java/inspection/TechnicalDebtVisitor.java
```

The delivery archive includes `.git` so the commit history survives extraction. Run Git commands inside `smart-code-inspection`. An ordinary GitHub source ZIP omits `.git`; cloning the published repository is preferable for reviewing history. Repository publishing and Moodle submission are separate from building the project. Submit the public repository URL and this README in Moodle; the assignment accepts README/PDF.
