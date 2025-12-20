package com.workvizo.ui.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class RoomCreationViewModel : ViewModel() {

    var name = mutableStateOf("")
    var description = mutableStateOf("")
    var startDate = mutableStateOf("")
    var endDate = mutableStateOf("")
    var people = mutableStateOf("1")
    var password = mutableStateOf("")

    // 🔥 THIS LINE MUST EXIST 🔥
    val tasks = mutableStateListOf<TaskDraft>()
}
