package com.aktepetugce.apkdownloader.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aktepetugce.apkdownloader.data.File

class MainViewModel : ViewModel() {
    val file = MutableLiveData(
        File(
             "", "", null
        )
    )
}