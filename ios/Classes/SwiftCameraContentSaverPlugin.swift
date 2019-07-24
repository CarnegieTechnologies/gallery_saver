import Flutter
import UIKit

public class SwiftCameraContentSaverPlugin: NSObject, FlutterPlugin {
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "camera_content_saver", binaryMessenger: registrar.messenger())
        let instance = SwiftCameraContentSaverPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if FlutterMethodCall.method == "startVideo"{
        }else {
            result("iOS jelena" + UIDevice.current.systemVersion)}
    }
    
    
    func saveImage(result: FlutterResult) {
    }
    
}
