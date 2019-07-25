import Flutter
import UIKit
import Photos

public class SwiftCameraContentSaverPlugin: NSObject, FlutterPlugin {
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "camera_content_saver", binaryMessenger: registrar.messenger())
        let instance = SwiftCameraContentSaverPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if call.method == "saveImage" {
            self.saveImage(result: result, call: call)
        } else if call.method == "saveVideo" {
            self.saveVideo(result: result, call: call)
        } else {
            result(FlutterMethodNotImplemented)
        }
    }
    
    func saveImage(result: @escaping FlutterResult, call: FlutterMethodCall) {
        let args = call.arguments as? Dictionary<String, Any>
        let typedData = args!["fileData"] as! FlutterStandardTypedData
        
        let image: UIImage = convertBytesToImage(bytes: typedData.data)
        
        let status = PHPhotoLibrary.authorizationStatus()
        if status == .notDetermined {
            PHPhotoLibrary.requestAuthorization({status in
                if status == .authorized{
                    self.performSaving(image: image, flutterResult: result)
                } else {
                    result("permission denied");
                }
            })
            
        } else if status == .authorized {
            self.performSaving(image: image, flutterResult: result)
        } else {
            result("please grant photos access");
        }
    }
    
    func performSaving(image: UIImage, flutterResult: @escaping FlutterResult){
        PHPhotoLibrary.shared().performChanges({
            PHAssetChangeRequest.creationRequestForAsset(from: image)
        }) { (success, error) in
            if success {
              flutterResult("success yeah")
            } else {
                flutterResult("failed to save image")
            }
        }
    }
    
    func saveVideo(result: FlutterResult, call: FlutterMethodCall) {
        result("save video")
    }
    
    func  convertBytesToImage(bytes: Data)  -> UIImage {
        return UIImage(data: bytes)!
    }
    
}


