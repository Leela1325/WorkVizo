package com.workvizo.ui.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class UserSessionViewModel : ViewModel() {
    var userId = mutableStateOf("")
}
