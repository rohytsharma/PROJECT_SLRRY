package com.example.slrry_10

import com.example.slrry_10.auth.canProceedFromPassword
import com.example.slrry_10.model.LocationModel
import com.example.slrry_10.repository.LocationRepository
import com.example.slrry_10.repository.UserRepo
import com.example.slrry_10.viewmodel.StartRunViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class AuthAndLogicUnitTest {

    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun canProceedFromPassword_validatesCorrectly() {
        assertFalse(canProceedFromPassword(password = "", confirm = ""))
        assertFalse(canProceedFromPassword(password = "abc", confirm = "xyz"))
        assertTrue(canProceedFromPassword(password = "pass123", confirm = "pass123"))
    }

    @Test
    fun finishRun_callsUserRepoSaveRunSession() = runTest {
        val userRepo = mock<UserRepo>()
        val locationRepo = mock<LocationRepository>()
        val vm = StartRunViewModel(userRepo = userRepo, locationRepo = locationRepo)

        vm.startTracking()
        vm.finishRun()

        verify(userRepo).saveRunSession(any())
    }

    @Test
    fun finishAreaCapture_addsCapturedArea_whenThreeOrMorePoints() {
        val userRepo = mock<UserRepo>()
        val locationRepo = mock<LocationRepository>()
        whenever(locationRepo.calculateArea(any())).thenReturn(123.0)
        val vm = StartRunViewModel(userRepo = userRepo, locationRepo = locationRepo)

        vm.startAreaCapture()
        vm.addPointToArea(LocationModel(10.0, 10.0))
        vm.addPointToArea(LocationModel(10.0, 10.001))
        vm.addPointToArea(LocationModel(10.001, 10.001))
        vm.finishAreaCapture()

        val captured = vm.uiState.value.capturedAreas
        assertEquals(1, captured.size)
        assertEquals(123.0, captured.first().area, 0.0001)
    }
}

