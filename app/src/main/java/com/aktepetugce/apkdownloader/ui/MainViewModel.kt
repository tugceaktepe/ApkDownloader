package com.aktepetugce.apkdownloader.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.aktepetugce.apkdownloader.data.model.File
import com.aktepetugce.apkdownloader.util.FileParams
import com.aktepetugce.apkdownloader.workers.DownloadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

const val TAG_OUTPUT = "OUTPUT"

@HiltViewModel
class MainViewModel @Inject constructor(private val workManager: WorkManager) : ViewModel() {

    internal val outputWorkInfos: LiveData<List<WorkInfo>> = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)

    val file = MutableLiveData(
        File(
             "", "", null
        )
    )

    fun startDownloadingFile(url : String) {
        val file = File(
            type = "APK",
            url = url.trim(),
            downloadedUri = null
        )

        val data = Data.Builder()
        data.apply {
            putString(FileParams.KEY_FILE_URL, file.url)
            putString(FileParams.KEY_FILE_TYPE, file.type)
        }

        //constraints
        /*val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .setRequiresBatteryNotLow(true)
            .build()*/

        //worker request
        val fileDownloadRequest = OneTimeWorkRequestBuilder<DownloadWorker>()
            //.setConstraints(constraints)
            .setInputData(data.build())
            .addTag(TAG_OUTPUT)
            .build()

        //start download worker
        workManager.beginUniqueWork(
            "oneFileDownloadWork",
            ExistingWorkPolicy.KEEP,
            fileDownloadRequest
        ).enqueue()
    }

}