# UML diagrams

These Mermaid diagrams render directly on GitHub. Names and relationships refer to the Java implementation in `inspection`.

## Class diagram

```mermaid
classDiagram
direction TB
class ProjectElement {
  <<interface>>
  +accept(ProjectVisitor visitor) void
}
class ProjectVisitor {
  <<interface>>
  +visit(SourceCodeFile file) void
  +visit(TestFile file) void
  +visit(ConfigurationFile file) void
  +visit(DependencyFile file) void
  +visit(Directory directory) void
}
class SourceCodeFile {
  <<record>>
  +String name
  +String module
  +int linesOfCode
  +int complexity
  +boolean production
  +boolean hardcodedCredentials
  +int todoCount
  +accept(ProjectVisitor visitor) void
}
class TestFile {
  <<record>>
  +String name
  +int linesOfCode
  +int testCount
  +int disabledTests
  +Set~String~ testedModules
  +boolean hardcodedCredentials
  +accept(ProjectVisitor visitor) void
}
class ConfigurationFile {
  <<record>>
  +String name
  +int linesOfCode
  +boolean hardcodedCredentials
  +boolean insecureConfiguration
  +accept(ProjectVisitor visitor) void
}
class DependencyFile {
  <<record>>
  +String name
  +int linesOfCode
  +List~Dependency~ dependencies
  +accept(ProjectVisitor visitor) void
}
class Dependency {
  <<record>>
  +String name
  +String version
  +boolean vulnerable
}
class Directory {
  <<record>>
  +String name
  +List~ProjectElement~ children
  +accept(ProjectVisitor visitor) void
}
class TraversingVisitor {
  <<abstract>>
  +visit(Directory directory) void
  +visit(SourceCodeFile file) void
  +visit(TestFile file) void
  +visit(ConfigurationFile file) void
  +visit(DependencyFile file) void
}
class SecurityAuditVisitor {
  +issues() List
  +criticalIssues() long
  +score() int
  +report() String
}
class ProjectStatisticsVisitor {
  +totalFiles() long
  +linesOfCode() long
  +sourceFiles() long
  +testFiles() long
  +tests() long
  +dependencies() long
  +averageComplexity() double
  +testSourceRatio() double
  +report() String
}
class QualityGateVisitor {
  +securityPassed() boolean
  +complexityPassed() boolean
  +testsPassed() boolean
  +scopePassed() boolean
  +passed() boolean
  +report() String
}
class ComplianceVisitor {
  +violations() List
  +compliant() boolean
  +report() String
}
class TechnicalDebtVisitor {
  +minutes() long
  +report() String
}
ProjectElement <|.. SourceCodeFile
ProjectElement <|.. TestFile
ProjectElement <|.. ConfigurationFile
ProjectElement <|.. DependencyFile
ProjectElement <|.. Directory
Directory o-- "0..*" ProjectElement : children
DependencyFile o-- "0..*" Dependency : dependencies
ProjectVisitor <|.. TraversingVisitor
TraversingVisitor <|-- SecurityAuditVisitor
TraversingVisitor <|-- ProjectStatisticsVisitor
TraversingVisitor <|-- QualityGateVisitor
TraversingVisitor <|-- ComplianceVisitor
TraversingVisitor <|-- TechnicalDebtVisitor
ProjectElement ..> ProjectVisitor : accept parameter
ProjectVisitor ..> SourceCodeFile : visit parameter
ProjectVisitor ..> TestFile : visit parameter
ProjectVisitor ..> ConfigurationFile : visit parameter
ProjectVisitor ..> DependencyFile : visit parameter
ProjectVisitor ..> Directory : visit parameter
QualityGateVisitor *-- SecurityAuditVisitor : private security
QualityGateVisitor *-- ProjectStatisticsVisitor : private statistics
```

`Directory` uses aggregation because immutable children can be shared. `QualityGateVisitor` owns its private delegates. Java record components are shown as public accessor-visible properties; their generated backing fields are private. All concrete visitors override the relevant leaf overloads and inherit directory traversal. `Checks` and report value records are omitted for readability because they do not participate in dispatch.

## Sequence diagram

The source node is reached through a `ProjectElement` child reference. Directory traversal is inherited by the same security visitor instance; there is no second traversal object.

```mermaid
sequenceDiagram
participant Main
participant Root as root Directory
participant Src as src Directory
participant File as PaymentService SourceCodeFile
participant Audit as SecurityAuditVisitor
Main->>Root: accept(audit)
Root->>Audit: visit(root)
activate Audit
Note over Audit: inherited TraversingVisitor.visit(Directory)
Audit->>Src: accept(this)
Src->>Audit: visit(src)
Note over Audit: iterate src.children()
Audit->>File: accept(this)
Note over File: runtime dispatch selects SourceCodeFile.accept
File->>Audit: visit(this as SourceCodeFile)
Note over File,Audit: overload selected statically then visitor implementation dynamically
Audit->>Audit: credentials(name, true)
Audit->>Audit: add SEC001 and SEC004 findings
Audit-->>File: return
File-->>Audit: return
Audit-->>Src: return
Src-->>Audit: return
Note over Audit: continue other root children
Audit-->>Root: return
deactivate Audit
Root-->>Main: return
Main->>Audit: report()
Audit-->>Main: score and security findings
```
