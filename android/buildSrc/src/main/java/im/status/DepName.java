package im.status;
import org.gradle.api.artifacts.ResolvedArtifact;

public class DepName {
    public String id;
    public String scope;
    public String group;
    public String artifact;
    public String version;
    public String jarFile;
    public ResolvedArtifact resolvedArtifact;

    public DepName(ResolvedArtifact resolvedArtifact) {
        this.resolvedArtifact = resolvedArtifact;
        this.id = extractId(resolvedArtifact);
        String[] ids = this.id.split(":");
        this.group = ids[0];
        this.artifact = ids[1];
        this.version = ids[2];
        this.scope = "compileOnly";
        this.jarFile = resolvedArtifact.getFile().getAbsolutePath();
    }

    private String extractId(ResolvedArtifact resolvedArtifact) {
        String displayName = resolvedArtifact.getId().getDisplayName();
        return displayName.substring(displayName.indexOf("(")+1, displayName.length() -2);
    }

    @Override
    public String toString() {
        return "DepName{" +
                "id='" + id + '\'' +
                ", sc='" + scope + '\'' +
                ", g='" + group + '\'' +
                ", a='" + artifact + '\'' +
                ", v='" + version + '\'' +
                ", f='" + jarFile + '\'' +
                ", ra=" + resolvedArtifact +
                '}';
    }
}