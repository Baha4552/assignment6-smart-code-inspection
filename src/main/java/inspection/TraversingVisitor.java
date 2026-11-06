package inspection;
/** A visit to a directory owns traversal. Elements only dispatch, never traverse. */
public abstract class TraversingVisitor implements ProjectVisitor {
    @Override public void visit(Directory directory) {
        for (ProjectElement child : directory.children()) child.accept(this);
    }
    @Override public void visit(SourceCodeFile file) { }
    @Override public void visit(TestFile file) { }
    @Override public void visit(ConfigurationFile file) { }
    @Override public void visit(DependencyFile file) { }
}
