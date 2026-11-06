# Verification results

Verified locally on 6 November 2026 with OpenJDK 25.0.1 and Apache Maven 3.9.12 on Windows. Compilation targets Java 17 (`--release 17`). All generated class files have major version 61. Java 17 and Java 21 runtime checks are configured in GitHub Actions and are a separate verification step.

## Build and behavioral tests

Command: `mvn -B clean verify`.

| Suite | Tests | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| InspectionTest | 27 | 0 | 0 | 0 |
| TechnicalDebtTest | 4 | 0 | 0 | 0 |
| Total | 31 | 0 | 0 | 0 |

Maven compiled all sources with release 17 and ran all 31 tests successfully. The packaging phase was interrupted because downloading the optional compression dependency of the JAR plugin stalled. The executable deliverable was packaged from the verified classes using the JDK jar tool with inspection.Main as its entry point. A complete Maven packaging run is therefore not claimed. Surefire summary files are retained in this directory. The packaged application was executed using `java -jar target/smart-code-inspection-1.0.0.jar`; its actual output is captured in `demo-output.txt`. All documented fintech metrics and both gate outcomes were checked against that output.

## Structural checks

* All five required elements perform typed `visitor.visit(this)` dispatch.
* No `instanceof` or `switch` appears in production Java code.
* The additional visitor's separate commit does not modify any of the five existing element classes or ProjectVisitor.
* Directory traversal and empty inputs are covered by assertions; recursion is exercised at 200 levels.
* All 31 tests use behavioral assertions; no assertion merely checks non-null existence.
* `git diff --check` passes.
* The final repository has at least eight meaningful commits. Git history is preserved in the delivery archive and the Git bundle.

UML relationships and sequence messages were checked against the implemented interfaces and traversal ownership. The diagrams are Mermaid source rendered by GitHub, not screenshots of an IDE.
