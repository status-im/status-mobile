package im.status

import com.android.build.gradle.internal.AndroidAsciiReportRenderer;
import com.android.build.gradle.internal.variant.BaseVariantData;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.logging.StyledTextOutputFactory;

public class DepReport extends DefaultTask {
    private AndroidAsciiReportRenderer renderer = new AndroidAsciiReportRenderer();
    private Set<BaseVariantData> variants = [];
    @TaskAction
    public void generate() throws IOException {
        renderer.setOutput(getServices().get(StyledTextOutputFactory.class).create(getClass()));
        SortedSet<BaseVariantData> sortedConfigurations = new TreeSet<BaseVariantData>(
                new Comparator<BaseVariantData>() {
                    public int compare(BaseVariantData conf1, BaseVariantData conf2) {
                        return conf1.getName().compareTo(conf2.getName());
                    }
                });
        sortedConfigurations.addAll(getVariants());
        for (BaseVariantData variant : sortedConfigurations) {
            renderer.startVariant(variant);
            renderer.render(variant);
        }
    }
    /**
     * Returns the configurations to generate the report for. Default to all configurations of
     * this task's containing project.
     *
     * @return the configurations.
     */
    public Set<BaseVariantData> getVariants() {
        return variants;
    }
    /**
     * Sets the configurations to generate the report for.
     *
     * @param configurations The configuration. Must not be null.
     */
    public void setVariants(Collection<BaseVariantData> variants) {
        this.variants.addAll(variants);
    }
}