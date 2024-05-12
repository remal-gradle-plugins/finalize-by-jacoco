package name.remal.gradle_plugins.finalize_by_jacoco;

import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
class FinalizeByJacocoPluginTest {

    final Project project;

    @BeforeEach
    void beforeEach() {
        project.getPluginManager().apply(FinalizeByJacocoPlugin.class);
    }

    @Test
    void test() {
        assertTrue(project.getPlugins().hasPlugin(FinalizeByJacocoPlugin.class));
    }

}
