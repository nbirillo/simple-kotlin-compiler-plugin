## A simple Kotlin compiler plugin example

This Kotlin compiler plugin generates a top level class:

```kotlin
public final class foo.bar.MyClass {
    fun foo(): String = "Hello world"
}
```

This plugin uses `1.8.20-dev-1278` bootstrap Kotlin version 
and should be used also with the same version of the compiler.

The plugin uses the [K2](https://kotlinlang.org/docs/whatsnew17.html#new-kotlin-k2-compiler-for-the-jvm-in-alpha) compiler.

Also, the plugin use the Kotlin compiler [test infrastructure](https://github.com/JetBrains/kotlin/tree/master/compiler/test-infrastructure).
The `compiler-plugin` module has a special task `generateTests` that generates simple box tests. 
Please, don't change the [generated tests](./compiler-plugin/src/test/java/org/example/compiler/plugin/runners) manually.

The [test resources' folder](./compiler-plugin/src/test/resources) contains also a dumped FIR and IR representations.