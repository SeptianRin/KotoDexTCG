package io.github.septianrin.kotodextcg.utils

import androidx.test.core.app.ApplicationProvider
import io.github.septianrin.kotodextcg.utils.createTestAppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.robolectric.DefaultTestLifecycle
import java.lang.reflect.Method

/**
 * The modern and correct way to manage setup and teardown for Robolectric tests.
 * This avoids all the 'Unresolved reference' errors from previous API versions.
 */
class KoinTestLifecycle : DefaultTestLifecycle() {

    /**
     * Called after the test environment is created but before the @Before methods.
     * This is the ideal place to set up dependency injection.
     */
    override fun prepareTest(test: Any) {
        super.prepareTest(test)

        startKoin {
            // Use ApplicationProvider to get the application context reliably.
            androidContext(ApplicationProvider.getApplicationContext())
            modules(createTestAppModule())
        }
    }

    /**
     * Called after the test method and @After methods have run.
     * This is the ideal place to perform cleanup.
     */
    override fun afterTest(method: Method?) {
        stopKoin()
        super.afterTest(method)
    }
}