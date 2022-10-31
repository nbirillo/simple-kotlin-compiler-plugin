package org.example.compiler.plugin

import org.example.compiler.plugin.runners.AbstractBoxTest
import org.jetbrains.kotlin.generators.generateTestGroupSuiteWithJUnit5

private const val TEST_DATA_ROOT = "compiler-plugin/src/test/resources/org/example/compiler/plugin"

// The main entry point for tests generation
// Run this command to generate all compiler tests
fun main() {
    generateTestGroupSuiteWithJUnit5 {
        testGroup(
            testDataRoot = "$TEST_DATA_ROOT/code-gen",
            testsRoot = "compiler-plugin/src/test/java"
        ) {
            testClass<AbstractBoxTest> {
                model("box")
            }
        }
    }
}