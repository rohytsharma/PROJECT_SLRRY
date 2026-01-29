package com.example.slrry_10

import com.example.slrry_10.auth.AuthManager
import com.example.slrry_10.auth.AuthServiceLocator
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import com.google.firebase.auth.FirebaseUser
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.mockito.Mockito

@RunWith(AndroidJUnit4::class)
class StartScreenNavigationInstrumentedTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<StartScreenActivity>()

    @Test
    fun signIn_opensLoginActivity() {
        Intents.init()
        try {
            composeRule.onNodeWithText("Sign In").performClick()
            intended(hasComponent(LoginActivity::class.java.name))
        } finally {
            Intents.release()
        }
    }

    @Test
    fun signUp_opensPasswordActivity() {
        Intents.init()
        try {
            composeRule.onNodeWithText("SIGN Up").performClick()
            intended(hasComponent(PasswordActivity::class.java.name))
        } finally {
            Intents.release()
        }
    }
}

@RunWith(AndroidJUnit4::class)
class PasswordNavigationInstrumentedTest {

    @get:Rule(order = 0)
    val authRule: TestRule = object : TestRule {
        override fun apply(base: Statement, description: Description): Statement {
            return object : Statement() {
                override fun evaluate() {
                    val previous = AuthServiceLocator.authManager
                    val fakeUser = Mockito.mock(FirebaseUser::class.java).apply {
                        Mockito.`when`(uid).thenReturn("test_uid")
                        Mockito.`when`(email).thenReturn("test@example.com")
                        Mockito.`when`(displayName).thenReturn("Test User")
                    }
                    AuthServiceLocator.authManager = object : AuthManager {
                        override fun registerWithEmail(
                            email: String,
                            password: String,
                            onResult: (Result<FirebaseUser>) -> Unit
                        ) {
                            onResult(Result.success(fakeUser))
                        }

                        override fun loginWithEmail(
                            email: String,
                            password: String,
                            onResult: (Result<FirebaseUser>) -> Unit
                        ) {
                            onResult(Result.success(fakeUser))
                        }

                        override fun sendPasswordResetEmail(
                            email: String,
                            onResult: (Result<Unit>) -> Unit
                        ) {
                            onResult(Result.success(Unit))
                        }

                        override fun ensureUserDoc(
                            user: FirebaseUser,
                            displayName: String?,
                            onResult: (Result<Unit>) -> Unit
                        ) {
                            onResult(Result.success(Unit))
                        }
                    }
                    try {
                        base.evaluate()
                    } finally {
                        AuthServiceLocator.authManager = previous
                    }
                }
            }
        }
    }

    @get:Rule
    val composeRule = createAndroidComposeRule<PasswordActivity>()

    @Test
    fun next_withValidPasswords_opensEnterNameActivity() {
        Intents.init()
        try {
            val fields = composeRule.onAllNodes(hasSetTextAction())
            fields[0].performTextInput("testuser")
            fields[1].performTextInput("pass123")
            fields[2].performTextInput("pass123")

            composeRule.onNodeWithText("Next").performClick()
            intended(hasComponent(EnterNameActivity::class.java.name))
        } finally {
            Intents.release()
        }
    }
}

