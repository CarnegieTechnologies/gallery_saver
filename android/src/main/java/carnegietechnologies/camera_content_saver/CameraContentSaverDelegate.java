package carnegietechnologies.camera_content_saver;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Handler;

import androidx.core.app.ActivityCompat;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

/**
 * Class holding implementation of saving images and videos
 */
public class CameraContentSaverDelegate implements
        PluginRegistry.RequestPermissionsResultListener {

    private static final int REQUEST_EXTERNAL_IMAGE_STORAGE_PERMISSION = 2345;

    private static final String ALREADY_ACTIVE = "already_active";
    private static final String PLUGIN_ALREADY_ACTIVE = "camera content saver is already active";

    private static final String KEY_PATH = "path";

    private final Activity activity;

    private MethodChannel.Result pendingResult;
    private MethodCall methodCall;
    private boolean isImage;

    CameraContentSaverDelegate(Activity activity) {
        this.activity = activity;
    }


    /**
     * Saves image or video to device
     *
     * @param methodCall - method call
     * @param result     - result to be set when saving operation finishes
     * @param isImage    - tells if image or video should be saved
     */
    void saveFile(MethodCall methodCall, MethodChannel.Result result, boolean isImage) {
        if (!setPendingMethodCallAndResult(methodCall, result)) {
            finishWithError();
            return;
        }

        if (!(ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_IMAGE_STORAGE_PERMISSION);
            this.methodCall = methodCall;
            this.pendingResult = result;
            this.isImage = isImage;
            return;
        }
        if (isImage) {
            saveImage(methodCall);
        } else {
            saveVideo(methodCall);
        }
    }

    /**
     * Saves video with provided name and description to device
     *
     * @param methodCall - method call
     */
    private void saveVideo(MethodCall methodCall) {
        String tempPath = methodCall.argument(KEY_PATH) == null ? ""
                : methodCall.argument(KEY_PATH).toString();

        String filePath;

        filePath = FileUtils.insertVideo(activity.getContentResolver(),
                tempPath);

        finishWithSuccess(filePath);
    }

    /**
     * Saves image with provided image and description to device
     *
     * @param methodCall - method call
     */
    private void saveImage(MethodCall methodCall) {
        final String tempPath = methodCall.argument(KEY_PATH) == null ? ""
                : methodCall.argument(KEY_PATH).toString();
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String path = FileUtils.insertImage(
                        activity.getContentResolver(), tempPath);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        finishWithSuccess(path);
                    }
                });
            }
        }).start();
    }

    private boolean setPendingMethodCallAndResult(
            MethodCall methodCall, MethodChannel.Result result) {
        if (pendingResult != null) {
            return false;
        }

        this.methodCall = methodCall;
        pendingResult = result;
        return true;
    }

    private void finishWithError() {
        pendingResult.error(CameraContentSaverDelegate.ALREADY_ACTIVE,
                CameraContentSaverDelegate.PLUGIN_ALREADY_ACTIVE, null);
        clearMethodCallAndResult();
    }

    private void finishWithSuccess(String imagePath) {
        pendingResult.success(imagePath);
        clearMethodCallAndResult();
    }

    private void clearMethodCallAndResult() {
        methodCall = null;
        pendingResult = null;
    }

    @Override
    public boolean onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        boolean permissionGranted =
                grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;

        if (requestCode == REQUEST_EXTERNAL_IMAGE_STORAGE_PERMISSION) {
            if (permissionGranted) {
                if (isImage) {
                    saveImage(methodCall);
                } else {
                    saveVideo(methodCall);
                }
            }
        } else {
            return false;
        }
        return true;
    }
}