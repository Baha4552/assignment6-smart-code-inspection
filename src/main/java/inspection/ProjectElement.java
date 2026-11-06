package inspection;
public interface ProjectElement {
    void accept(ProjectVisitor visitor);
}
