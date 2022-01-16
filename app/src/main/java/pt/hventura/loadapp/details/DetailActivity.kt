package pt.hventura.loadapp.details

import android.app.NotificationManager
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import pt.hventura.loadapp.R
import pt.hventura.loadapp.chooseFile.MainActivity
import pt.hventura.loadapp.databinding.ActivityDetailBinding
import pt.hventura.loadapp.utilities.cancelNotifications

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private lateinit var fileName: String
    private lateinit var fileStatus: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_detail)
        intent.extras?.let {
            if (it.getString("file_name")!!.isNotBlank()) {
                fileName = it.getString("file_name")!!
            }
            if (it.getString("file_status")!!.isNotBlank()) {
                fileStatus = it.getString("file_status")!!
            }
        }
        val notificationManager = ContextCompat.getSystemService(applicationContext, NotificationManager::class.java) as NotificationManager
        notificationManager.cancelNotifications()

        binding.contentDetail.name = fileName
        binding.contentDetail.status = fileStatus

        binding.detailsOkButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            binding.contentDetail.name = ""
            binding.contentDetail.status = ""
        }

    }

}