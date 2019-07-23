package carnegietechnologies.camera_content_saver;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;


import java.io.IOException;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry;

public class CameraContentSaverDelegate implements
        PluginRegistry.RequestPermissionsResultListener {

    static final int REQUEST_EXTERNAL_IMAGE_STORAGE_PERMISSION = 2345;

    static final String KEY_DATA = "fileData";
    static final String KEY_NAME = "title";
    static final String KEY_DESCRIPTION = "description";

    static final String DEFAULT_NAME = "cameraImage";
    static final String DEFAULT_DESCRIPTION = "cameraDescription";

    private final Activity activity;

    private MethodChannel.Result pendingResult;
    private MethodCall methodCall;

    public CameraContentSaverDelegate(Activity activity) {
        this.activity = activity;
    }


    public void saveImage(MethodCall methodCall, MethodChannel.Result result) {
        if (!setPendingMethodCallAndResult(methodCall, result)) {
            finishWithAlreadyActiveError();
            return;
        }

        if (!(ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_EXTERNAL_IMAGE_STORAGE_PERMISSION);
            this.methodCall = methodCall;
            this.pendingResult = result;
            return;
        }
        saveToGallery(methodCall);
    }

    private void saveToGallery(MethodCall methodCall) {
        byte[] fileData = methodCall.argument(KEY_DATA);
        String title = methodCall.argument(KEY_NAME) == null ? DEFAULT_NAME
                : methodCall.argument("title").toString();

        String description = methodCall.argument(KEY_DESCRIPTION) == null ? DEFAULT_DESCRIPTION
                : methodCall.argument("description").toString();

        String filePath = null;

        try {
            filePath = FileUtils.insertImage(activity.getContentResolver(),
                    fileData, title, description);
        } catch (IOException e) {
            e.printStackTrace();
        }

        finishWithSuccess(filePath);
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

    private void finishWithAlreadyActiveError() {
        finishWithError("already_active", "Image picker is already active");
    }

    private void finishWithError(String errorCode, String errorMessage) {
        pendingResult.error(errorCode, errorMessage, null);
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

        switch (requestCode) {
            case REQUEST_EXTERNAL_IMAGE_STORAGE_PERMISSION:
                if (permissionGranted) {
                    saveToGallery(methodCall);
                }
                break;
            default:
                return false;
        }
        return true;
    }

}
