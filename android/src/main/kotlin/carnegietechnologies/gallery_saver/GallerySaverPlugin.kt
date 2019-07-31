package carnegietechnologies.gallery_saver

import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry.Registrar

class GallerySaverPlugin private constructor(
        private val cameraContentSaver: GallerySaver) : MethodCallHandler {

  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(),
              "gallery_saver")
      val cameraContentSaver = GallerySaver(registrar.activity())
      registrar.addRequestPermissionsResultListener(cameraContentSaver)
      val instance = GallerySaverPlugin(
              cameraContentSaver)
      channel.setMethodCallHandler(instance)
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "saveImage" -> cameraContentSaver.saveFile(call, result, true)
      "saveVideo" -> cameraContentSaver.saveFile(call, result, false)
      else -> result.notImplemented()
    }
  }
}
