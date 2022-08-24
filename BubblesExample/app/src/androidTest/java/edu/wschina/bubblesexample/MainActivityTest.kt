package edu.wschina.bubblesexample

import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import edu.wschina.bubblesexample.View.MainActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testFunction() {
        //onView(withId(R.id.txtInputMessage)).perform(click())
    }
}