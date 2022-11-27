package com.aktepetugce.apkdownloader.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.webkit.URLUtil
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.work.*
import com.aktepetugce.apkdownloader.BuildConfig
import com.aktepetugce.apkdownloader.databinding.ActivityMainBinding
import com.aktepetugce.apkdownloader.util.FileParams
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val viewModel : MainViewModel by viewModels()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        checkFileExist()

        with(binding) {
            btnDownload.setOnClickListener {
                val trimmedUrlText = urlText.text.toString().trim()
                if(URLUtil.isValidUrl(trimmedUrlText)) {
                    viewModel.startDownloadingFile(trimmedUrlText)
                }else{
                    Toast.makeText(this@MainActivity, "Url is not valid: ${urlText.text.toString().trim()}", Toast.LENGTH_LONG).show()
                }
            }
            btnNext.setOnClickListener {
                Intent(this@MainActivity, SecondActivity::class.java).apply {
                    startActivity(this)
                }
            }
            btnInstall.setOnClickListener {
                installApk()
            }

            viewModel.outputWorkInfos.observe(this@MainActivity, workInfosObserver())
        }
    }

    private fun workInfosObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->

                if (listOfWorkInfo.isNullOrEmpty()) {
                    return@Observer
                }
                val workInfo = listOfWorkInfo[0]
                when (workInfo.state) {
                    WorkInfo.State.ENQUEUED -> {
                        binding.btnInstall.isEnabled = false
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        onSuccess(workInfo.outputData.getString(FileParams.KEY_FILE_URI) ?: "")
                    }
                    WorkInfo.State.FAILED -> {
                        onFailure("Downloading failed!")
                    }
                    WorkInfo.State.RUNNING -> {
                        //
                    }
                    else -> {
                        //failed("Something went wrong")
                    }
                }
          }
    }

    private fun checkFileExist(){
        var destination =getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += "app.apk"
        val file = java.io.File(destination)
        if (file.exists()) {
            binding.apkExist.text = "New Apk Downloaded : YES"
        }else{
            binding.apkExist.text = "New Apk Downloaded : NO"
        }
    }

    private fun installApk(){

        var destination =
            getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += "app.apk"

        val intent = Intent(Intent.ACTION_VIEW)
        val uri = uriFromFile(java.io.File(destination))
        intent.setDataAndType(
            uri,
            "application/vnd.android.package-archive"
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("TAG", "Error in opening the file!")
        }
    }

    private fun uriFromFile( file: java.io.File): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
           FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", file)
        } else {
            Uri.fromFile(file)
        }
    }

    private fun onSuccess(uri: String) {
        viewModel.file.value = viewModel.file.value?.copy(downloadedUri = uri)
        binding.apkExist.text = "New Apk Downloaded : YES"
        binding.btnInstall.isEnabled = true
        Toast.makeText(this, uri, Toast.LENGTH_LONG).show()
    }
    private fun onFailure(message: String){
        binding.btnInstall.isEnabled = false
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}