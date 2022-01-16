package pt.hventura.loadapp.chooseFile

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.webkit.URLUtil
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import pt.hventura.loadapp.R
import pt.hventura.loadapp.databinding.ActivityMainBinding
import pt.hventura.loadapp.ui.LoadingButton
import pt.hventura.loadapp.utilities.UtilityFunctions.getFileNameFromURL
import pt.hventura.loadapp.utilities.UtilityFunctions.hideKeyboard
import pt.hventura.loadapp.utilities.sendNotification
import timber.log.Timber
import java.io.FileNotFoundException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var notificationManager: NotificationManager
    private lateinit var loadingButton: LoadingButton
    private var noOptions: Boolean = false
    private var downloadID: Long = 0
    private var url = ""
    private var notificationMessage = ""
    private var fileName = ""
    private var file: ParcelFileDescriptor? = null // Needed for using with another app or interaction with the file it self(*)
    private var downloadManager: DownloadManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.plant(Timber.DebugTree())

        // INIT
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        viewModel = ViewModelProvider(this)[MainViewModel::class.java]
        binding.lifecycleOwner = this
        binding.contentMain.viewModel = viewModel
        setSupportActionBar(binding.toolbar)
        notificationManager = ContextCompat.getSystemService(applicationContext, NotificationManager::class.java) as NotificationManager
        loadingButton = binding.contentMain.customButton
        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
        createChannel(
            getString(R.string.download_notification_channel_id),
            getString(R.string.download_notification_channel_name)
        )

        // CONTROL FLOW - CLICKS AND CHANGES
        binding.contentMain.downloadOption.setOnCheckedChangeListener { _, i ->
            noOptions = false
            when (i) {
                R.id.option1 -> {
                    url = resources.getString(R.string.option1_url)
                    notificationMessage = resources.getString(R.string.option1_notification_description)
                    fileName = resources.getString(R.string.option1_text)
                }
                R.id.option2 -> {
                    url = resources.getString(R.string.option2_url)
                    notificationMessage = resources.getString(R.string.option2_notification_description)
                    fileName = resources.getString(R.string.option2_text)
                }
                R.id.option3 -> {
                    url = resources.getString(R.string.option3_url)
                    notificationMessage = resources.getString(R.string.option3_notification_description)
                    fileName = resources.getString(R.string.option3_text)
                }
                else -> {
                    url = ""
                    notificationMessage = ""
                }
            }
            if (url.isNotBlank()) {
                binding.contentMain.customUrlValue.setText("")
                if (!noOptions) {
                    binding.contentMain.customUrl.clearFocus()
                }
                hideKeyboard(this, binding.root)
            }
        }

        binding.contentMain.customUrlValue.doOnTextChanged { _, _, _, count ->
            if (count > 0) {
                if (!noOptions) {
                    binding.contentMain.downloadOption.clearCheck()
                    noOptions = true
                }
            }
        }

        loadingButton.setOnClickListener {
            if (noOptions) {
                url = binding.contentMain.customUrlValue.text.toString()
                notificationMessage = resources.getString(R.string.custom_url_notification_description)
                fileName = getFileNameFromURL(url)
            }

            if (url == "") {
                Toast.makeText(this, "Please select a valid option", Toast.LENGTH_SHORT).show()
            } else {
                loadingButton.initLoading()
                download()
            }
        }

    }

    /**
     * FROM: https://developer.android.com/guide/components/broadcasts#context-registered-receivers
     * (Point 3)
     * Be mindful of where you register and unregister the receiver, for example,
     * if you register a receiver in onCreate(Bundle) using the activity's context,
     * you should unregister it in onDestroy() to prevent leaking the receiver out
     * of the activity context.
     * If you register a receiver in onResume(), you should unregister it in onPause()
     * to prevent registering it multiple times (If you don't want to receive broadcasts
     * when paused, and this can cut down on unnecessary system overhead).
     * Do not unregister in onSaveInstanceState(Bundle),
     * because this isn't called if the user moves back in the history stack.
     * */
    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            /**
             * After some research there is a way to get the status of download through Cursor.
             * I've read this -> http://android-coding.blogspot.com/2011/11/download-using-downloadmanager.html
             * and -> https://developer.android.com/reference/android/app/DownloadManager.html#query(android.app.DownloadManager.Query)
             * */
            val query = id?.let { DownloadManager.Query().setFilterById(it) }
            val cursor = downloadManager!!.query(query)

            /**
             * Move the cursor to the first row.
             * This method will return false if the cursor is empty.
             * */
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                when (cursor.getInt(columnIndex)) {
                    DownloadManager.STATUS_SUCCESSFUL -> {
                        try {
                            // Needed for using with another app or interaction with the file it self(*)
                            // file = downloadManager!!.openDownloadedFile(downloadID)
                            if (downloadID == id) {
                                loadingButton.downloadComplete()
                                notificationManager.sendNotification(notificationMessage, this@MainActivity, fileName, "Success")
                                return
                            }
                        } catch (e: FileNotFoundException) {
                            Timber.e(e.message)
                        }

                    }
                    DownloadManager.STATUS_FAILED -> {
                        // Custom URL always get FAILED STATUS ... cannot figure it out why :(
                        loadingButton.downloadComplete()
                        notificationManager.sendNotification(notificationMessage, this@MainActivity, fileName, "Fail")
                    }
                }
                binding.contentMain.downloadOption.clearCheck()
                binding.contentMain.customUrlValue.setText("")
                binding.contentMain.customUrl.clearFocus()
                hideKeyboard(this@MainActivity, binding.root)
            }
        }
    }

    private fun download() {
        val request =
            DownloadManager.Request(Uri.parse(url))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    URLUtil.guessFileName(url, null, null)
                ) // from here: https://developer.android.com/reference/kotlin/android/webkit/URLUtil#guessfilename


        downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        if (downloadManager != null) {
            downloadID = downloadManager!!.enqueue(request)// enqueue puts the download request in the queue.
        } else {
            Toast.makeText(this, "Something when wrong with the system\nDownload Manager not instantiated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createChannel(channelId: String, channelName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setShowBadge(false)
            }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "File Download Ready"

            val notificationManager = this.getSystemService(
                NotificationManager::class.java
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}