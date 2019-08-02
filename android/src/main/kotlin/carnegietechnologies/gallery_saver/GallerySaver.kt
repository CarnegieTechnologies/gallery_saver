package carnegietechnologies.gallery_saver

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.PluginRegistry
import kotlinx.coroutines.*

/**
 * Class holding implementation of saving images and videos
 */
class GallerySaver internal constructor(private val activity: Activity) : PluginRegistry.RequestPermissionsResultListener {

    private var pendingResult: MethodChannel.Result? = null
    private var methodCall: MethodCall? = null
    private var isImage: Boolean = false

    private val job = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    /**
     * Saves image or video to device
     *
     * @param methodCall - method call
     * @param result     - result to be set when saving operation finishes
     * @param isImage    - tells if image or video should be saved
     */
    internal fun saveFile(methodCall: MethodCall, result: MethodChannel.Result, isImage: Boolean) {
        if (!setPendingMethodCallAndResult(methodCall, result)) {
            finishWithError()
            return
        }

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_EXTERNAL_IMAGE_STORAGE_PERMISSION)
            this.methodCall = methodCall
            this.pendingResult = result
            this.isImage = isImage
            return
        }
        if (isImage) {
            saveImage(methodCall)
        } else {
            saveVideo(methodCall)
        }
    }

    /**
     * Saves video with provided name and description to device
     *
     * @param methodCall - method call
     */
    private fun saveVideo(methodCall: MethodCall) {
        val tempPath = if (methodCall.argument<Any>(KEY_PATH) == null)
            ""
        else
            methodCall.argument<Any>(KEY_PATH)!!.toString()

        uiScope.launch {
            val path = async(Dispatchers.IO) {
                FileUtils.insertVideo(activity.contentResolver, tempPath)
            }
            finishWithSuccess(path.await())
        }
    }


    /**
     * Saves image with provided image and description to device
     *
     * @param methodCall - method call
     */
    private fun saveImage(methodCall: MethodCall) {
        val tempPath = if (methodCall.argument<Any>(KEY_PATH) == null)
            ""
        else
            methodCall.argument<Any>(KEY_PATH).toString()

        uiScope.launch {
            val path = async(Dispatchers.IO) {
                FileUtils.insertImage(activity.contentResolver, tempPath)
            }
            finishWithSuccess(path.await())
        }
    }

    private fun setPendingMethodCallAndResult(
            methodCall: MethodCall, result: MethodChannel.Result): Boolean {
        if (pendingResult != null) {
            return false
        }

        this.methodCall = methodCall
        pendingResult = result
        return true
    }

    private fun finishWithError() {
        pendingResult!!.error(ALREADY_ACTIVE, PLUGIN_ALREADY_ACTIVE, null)
        clearMethodCallAndResult()
    }

    private fun finishWithSuccess(imagePath: String) {
        pendingResult!!.success(imagePath)
        clearMethodCallAndResult()
    }

    private fun clearMethodCallAndResult() {
        methodCall = null
        pendingResult = null
    }

    override fun onRequestPermissionsResult(
            requestCode: Int, permissions: Array<String>, grantResults: IntArray): Boolean {
        val permissionGranted = grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED

        if (requestCode == REQUEST_EXTERNAL_IMAGE_STORAGE_PERMISSION) {
            if (permissionGranted) {
                if (isImage) {
                    saveImage(methodCall!!)
                } else {
                    saveVideo(methodCall!!)
                }
            }
        } else {
            return false
        }
        return true
    }

    companion object {

        private const val REQUEST_EXTERNAL_IMAGE_STORAGE_PERMISSION = 2345

        private const val ALREADY_ACTIVE = "already_active"
        private const val PLUGIN_ALREADY_ACTIVE = "camera content saver is already active"

        private const val KEY_PATH = "path"
    }
}