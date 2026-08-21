package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ThemeMode
import com.example.model.UserRole
import com.example.model.UserSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthenticationViewModel : ViewModel() {
    private val _currentUser = MutableStateFlow<UserSession?>(
        UserSession(
            id = "tch_01",
            username = "teacher01",
            fullName = "Prof. Sarah Jenkins",
            role = UserRole.TEACHER,
            email = "s.jenkins@stjosephs.edu",
            homeroomClass = "10-A",
            subjects = listOf("Physics", "Science Lab")
        )
    )
    val currentUser: StateFlow<UserSession?> = _currentUser.asStateFlow()

    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun switchRole(role: UserRole) {
        viewModelScope.launch {
            _currentUser.value = when (role) {
                UserRole.STUDENT -> UserSession(
                    id = "std_01",
                    username = "student01",
                    fullName = "Alex Johnson",
                    role = UserRole.STUDENT,
                    email = "alex.j@stjosephs.edu",
                    grade = "10",
                    section = "A",
                    rollNumber = "01",
                    house = "St. Patrick",
                    bloodGroup = "O+"
                )
                UserRole.TEACHER -> UserSession(
                    id = "tch_01",
                    username = "teacher01",
                    fullName = "Prof. Sarah Jenkins",
                    role = UserRole.TEACHER,
                    email = "s.jenkins@stjosephs.edu",
                    homeroomClass = "10-A",
                    subjects = listOf("Physics", "Science Lab")
                )
                UserRole.STAFF -> UserSession(
                    id = "staff_01",
                    username = "staff01",
                    fullName = "Mr. Thomas Wright",
                    role = UserRole.STAFF,
                    email = "t.wright@stjosephs.edu"
                )
                UserRole.ADMIN -> UserSession(
                    id = "admin_01",
                    username = "admin01",
                    fullName = "Dr. Arthur Pendelton",
                    role = UserRole.ADMIN,
                    email = "principal@stjosephs.edu"
                )
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    fun logout() {
        _currentUser.value = null
    }

    fun loginWithPreset(role: UserRole) {
        switchRole(role)
    }
}
