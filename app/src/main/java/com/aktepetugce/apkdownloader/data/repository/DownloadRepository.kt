package com.aktepetugce.apkdownloader.data.repository

import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject

class DownloadRepository @Inject constructor(private val downloadService: DownloadService) {
    suspend fun downloadApk(url: String) : Response<ResponseBody> {
       return downloadService.downloadFile(url)
    }
}