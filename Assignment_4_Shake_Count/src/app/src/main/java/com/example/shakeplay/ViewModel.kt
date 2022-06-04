package com.example.shakeplay

import androidx.lifecycle.ViewModel

class CountViewModel : ViewModel() {

    var shakeCount = 0
    var limitReached = false
}
