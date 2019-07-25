package carnegietechnologies.camera_content_saver;

import androidx.annotation.NonNull;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * CameraContentSaverPlugin - entry point for android part of plugin
 */
public class CameraContentSaverPlugin implements MethodCallHandler {

    private CameraContentSaverDelegate cameraContentSaverDelegate;

    private CameraContentSaverPlugin(CameraContentSaverDelegate delegate) {
        this.cameraContentSaverDelegate = delegate;
    }

    /**
     * Plugin registration.
     */
    public static void registerWith(Registrar registrar) {
        final MethodChannel channel = new MethodChannel(registrar.messenger(),
                "camera_content_saver");
        CameraContentSaverDelegate cameraContentSaverDelegate =
                new CameraContentSaverDelegate(registrar.activity());

        registrar.addRequestPermissionsResultListener(cameraContentSaverDelegate);

        final CameraContentSaverPlugin instance = new CameraContentSaverPlugin(
                cameraContentSaverDelegate);
        channel.setMethodCallHandler(instance);
    }

    @Override
    public void onMethodCall(@NonNull MethodCall methodCall, @NonNull Result result) {
        switch (methodCall.method) {
            case "saveImage":
                cameraContentSaverDelegate.saveFile(methodCall, result, true);
                break;
            case "saveVideo":
                cameraContentSaverDelegate.saveFile(methodCall, result, false);
                break;
            default:
                result.notImplemented();
                break;
        }
    }
}
