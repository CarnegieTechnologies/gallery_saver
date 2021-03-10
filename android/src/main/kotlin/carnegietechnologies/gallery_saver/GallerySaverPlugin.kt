package carnegietechnologies.gallery_saver

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class GallerySaverPlugin private constructor(
        private val gallerySaver: GallerySaver) : MethodCallHandler {

    companion object {
        @JvmStatic
        fun registerWith(registrar: Registrar) {
            if (registrar.activity() == null) {
                // If a background flutter view tries to register the plugin,
                // there will be no activity from the registrar,
                // we stop the registering process immediately
                // because the GallerySaver requires an activity.
                return
            }

            val channel = MethodChannel(registrar.messenger(),
                    "gallery_saver")
            val gallerySaver = GallerySaver(registrar.activity())
            registrar.addRequestPermissionsResultListener(gallerySaver)
            val instance = GallerySaverPlugin(
                    gallerySaver)
            channel.setMethodCallHandler(instance)
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result) {
        when (call.method) {
            "saveImage" -> gallerySaver.checkPermissionAndSaveFile(call, result, MediaType.image)
            "saveVideo" -> gallerySaver.checkPermissionAndSaveFile(call, result, MediaType.video)
            else -> result.notImplemented()
        }
    }
}
