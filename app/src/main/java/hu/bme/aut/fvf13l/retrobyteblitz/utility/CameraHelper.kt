package hu.bme.aut.fvf13l.retrobyteblitz.utility

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class CameraHelper(private val context: Context) {

    companion object {
        const val REQUEST_CAMERA_PERMISSION = 1001
        const val REQUEST_CAMERA = 1002
    }

    // Check if camera permission is granted
    fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    // Request camera permission
    fun requestPermissions() {
        // Request only the CAMERA permission
        ActivityCompat.requestPermissions(
            context as Activity,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    // Launch the camera intent to capture a photo
    fun launchCamera() {
        if (checkPermissions()) {
            // Create an intent to open the camera
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            // Check if there is a camera activity available
            val activity = context as? Activity  // Cast context to Activity safely
            activity?.let {
                if (intent.resolveActivity(it.packageManager) != null) {
                    // Start the activity for result if available
                    it.startActivityForResult(intent, REQUEST_CAMERA)
                }
            }
        } else {
            // If no permission, request permission
            requestPermissions()
        }
    }


}

