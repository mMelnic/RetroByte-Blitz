package hu.bme.aut.fvf13l.retrobyteblitz.service

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.content.Context
import android.app.Activity
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraService : Service() {

    private val binder = CameraBinder()

    inner class CameraBinder : Binder() {
        fun getService(): CameraService = this@CameraService
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    fun checkPermissions(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(android.Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    fun launchCamera(activity: Activity) {
        if (checkPermissions(activity)) {
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            if (intent.resolveActivity(activity.packageManager) != null) {
                activity.startActivityForResult(intent, REQUEST_CAMERA)
            }
        } else {
            requestPermissions(activity)
        }
    }

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 1001
        const val REQUEST_CAMERA = 1002
    }
}
