package com.grensil.nhn_gmail

import org.junit.Assert.*
import org.junit.Test

class AppTest {

    @Test
    fun `MainActivity class exists`() {
        val activityClass = MainActivity::class.java
        assertNotNull(activityClass)
        assertTrue(androidx.activity.ComponentActivity::class.java.isAssignableFrom(activityClass))
    }

    @Test
    fun `NhnApplication class exists`() {
        val applicationClass = NhnApplication::class.java
        assertNotNull(applicationClass)
        assertTrue(android.app.Application::class.java.isAssignableFrom(applicationClass))
        
        val onCreateMethod = applicationClass.declaredMethods.find { it.name == "onCreate" }
        assertNotNull(onCreateMethod)
    }

    @Test
    fun `AppModule class exists if present`() {
        val appModuleClass = try {
            Class.forName("com.grensil.nhn_gmail.di.AppModule")
        } catch (_: ClassNotFoundException) {
            null
        }
        
        appModuleClass?.let { moduleClass ->
            assertNotNull(moduleClass)
            val methods = moduleClass.declaredMethods
            assertTrue(methods.isNotEmpty())
        }
    }

    @Test
    fun `ViewModelFactory class exists if present`() {
        val viewModelFactoryClass = try {
            Class.forName("com.grensil.nhn_gmail.di.ViewModelFactory")
        } catch (_: ClassNotFoundException) {
            null
        }
        
        viewModelFactoryClass?.let { factoryClass ->
            assertNotNull(factoryClass)
            assertTrue(androidx.lifecycle.ViewModelProvider.Factory::class.java.isAssignableFrom(factoryClass))
        }
    }

}