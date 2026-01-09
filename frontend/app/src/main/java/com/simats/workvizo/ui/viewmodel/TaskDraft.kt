package com.simats.workvizo.ui.viewmodel

import androidx.compose.runtime.mutableStateOf

class TaskDraft {
    var taskName = mutableStateOf("")
    var startDate = mutableStateOf("")
    var endDate = mutableStateOf("")
    var people = mutableStateOf("")
}
