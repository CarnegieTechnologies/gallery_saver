package carnegietechnologies.camera_content_saver;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * CameraContentSaverPlugin
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
    public void onMethodCall(MethodCall methodCall, Result result) {
        if (methodCall.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        } else if (methodCall.method.equals("saveImage")) {
            cameraContentSaverDelegate.saveImage(methodCall, result);
        } else {
            result.notImplemented();
        }
    }
}
