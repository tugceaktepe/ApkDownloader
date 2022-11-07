package com.aktepetugce.apkdownloader.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.work.*
import com.aktepetugce.apkdownloader.util.FileParams
import com.aktepetugce.apkdownloader.util.NotificationConstants
import com.aktepetugce.apkdownloader.R
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class DownloadWorker(private val context: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {

        val fileUrl = inputData.getString(FileParams.KEY_FILE_URL) ?: ""
        val fileType = inputData.getString(FileParams.KEY_FILE_TYPE) ?: ""

        Log.d("TAG", "doWork: $fileUrl | $fileType")

        deleteExistingFile()

        val progress = "Downloading"
        setForeground(createForegroundInfo(progress))

        val uri = downloadFile(
            fileType = fileType,
            fileUrl = fileUrl,
            context = context
        )

        return if (uri != null) {
            NotificationManagerCompat.from(context).cancel(NotificationConstants.NOTIFICATION_ID)
            Result.success(workDataOf(FileParams.KEY_FILE_URI to uri.toString()))
        } else {
            NotificationManagerCompat.from(context).cancel(NotificationConstants.NOTIFICATION_ID)
            Result.failure()
        }

    }

    // Creates an instance of ForegroundInfo which can be used to update the
    // ongoing notification.
    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val notificationId = NotificationConstants.NOTIFICATION_ID
        val title = context.getString(R.string.app_name)

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val cancel = "Cancel"
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(id)

        val notification = NotificationCompat.Builder(context, NotificationConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()


        return ForegroundInfo(notificationId, notification)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel(){
        val name = NotificationConstants.CHANNEL_NAME
        val description = NotificationConstants.CHANNEL_DESCRIPTION
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(NotificationConstants.CHANNEL_ID, name, importance)
        channel.description = description

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.createNotificationChannel(channel)
    }

    private fun deleteExistingFile() {
        var destination = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += "app.apk"
        val file = File(destination)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun downloadFile(
        fileType: String,
        fileUrl: String,
        context: Context
    ): Uri? {
        val mimeType = when (fileType) {
            "APK" -> "application/vnd.android.package-archive"
            else -> ""
        }

        if (mimeType.isEmpty()) return null

        var destination =
            context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString() + "/"
        destination += "app.apk"

        val target = File(
            destination
        )
        URL(fileUrl).openStream().use { input ->
            FileOutputStream(target).use { output ->
                input.copyTo(output)
            }
        }

        return target.toUri()
    }
}