**Tested on Java LTS versions from <!--property:java-runtime.min-version-->8<!--/property--> to <!--property:java-runtime.max-version-->21<!--/property-->.**

**Tested on Gradle versions from <!--property:gradle-api.min-version-->6.0<!--/property--> to <!--property:gradle-api.max-version-->8.12-rc-1<!--/property-->.**

# `name.remal.finalize-by-jacoco` plugin

[![configuration cache: supported](https://img.shields.io/static/v1?label=configuration%20cache&message=supported&color=success)](https://docs.gradle.org/current/userguide/configuration_cache.html)

By default, Gradle doesn't make Jacoco tasks executed automatically. This plugin fixes this.

This plugin applies [`jacoco` plugin](https://docs.gradle.org/current/userguide/jacoco_plugin.html) and then
makes [`Test`](https://docs.gradle.org/current/javadoc/org/gradle/api/tasks/testing/Test.html) task finalized
by corresponding [`JacocoReport`](https://docs.gradle.org/current/javadoc/org/gradle/testing/jacoco/tasks/JacocoReport.html)
and [`JacocoCoverageVerification`](https://docs.gradle.org/current/javadoc/org/gradle/testing/jacoco/tasks/JacocoCoverageVerification.html) tasks.

Also, `JacocoReport` and `JacocoCoverageVerification` become depend on the corresponding `Test` task.

Example:

* `test` is finalized by `jacocoTestReport` and `jacocoTestCoverageVerification` tasks
* if a user executes `jacocoTestReport` task, `test` will also be executed
* if a user executes `jacocoTestCoverageVerification` task, `test` will also be executed

## Additional types of tests

Additional test source sets are also supported.
See
[`jvm-test-suite`](https://docs.gradle.org/current/userguide/jvm_test_suite_plugin.html),
[`name.remal.test-source-sets`](https://plugins.gradle.org/plugin/name.remal.test-source-sets),
and similar plugins.

Example:

```groovy
testing.suites {
  test {
    useJUnitJupiter()
  }

  integrationTest(JvmTestSuite) {
  }
}
```

* `integrationTest` is finalized by `jacocoIntegrationTestReport` and `jacocoIntegrationTestCoverageVerification` tasks
* if a user executes `jacocoIntegrationTestReport` task, `integrationTest` will also be executed
* if a user executes `jacocoIntegrationTestCoverageVerification` task, `integrationTest` will also be executed

## How the plugin matches tasks

If a task has [`JacocoTaskExtension`](https://docs.gradle.org/current/javadoc/org/gradle/testing/jacoco/plugins/JacocoTaskExtension.html) extension,
Jacoco tasks that work with the same Jacoco execution data file will be matched.

If a Jacoco task works with multiple execution files, it won't be matched.
