package io.github.septianrin.kotodextcg.utils

import org.robolectric.RobolectricTestRunner
import org.robolectric.TestLifecycle

/**
 * Custom Robolectric Test Runner.
 * Its only job is to point to our custom TestLifecycle class.
 */
class KotoDexTestRunner(testClass: Class<*>) : RobolectricTestRunner(testClass) {

    /**
     * This is the correct method to override in modern Robolectric.
     * It tells Robolectric to use our KoinTestLifecycle class to manage the test environment.
     */
    override fun getTestLifecycleClass(): Class<out TestLifecycle<*>> {
        return KoinTestLifecycle::class.java
    }
}