import Flutter
import UIKit
import Photos

public class SwiftGallerySaverPlugin: NSObject, FlutterPlugin {
    let path = "path"
    let permissionDenied = "permission denied"
    let pleaseGrantAccess = "please grant photos access"
    let imageSaved = "image saved"
    let videoSaved = "videoSaved"
    let failedToSaveImage = "failed to save image"
    let failedToSaveVideo = "failed to save video"
    
    public static func register(with registrar: FlutterPluginRegistrar) {
        let channel = FlutterMethodChannel(name: "gallery_saver", binaryMessenger: registrar.messenger())
        let instance = SwiftGallerySaverPlugin()
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
    
    /// Tries to save image to the photos app.
    /// If user hasn't already permitted saving to the photos, it will be requested
    /// to do so.
    ///
    /// - Parameters:
    ///   - result: flutter result that gets sent back to the dart code
    ///   - call: method object with params for saving image
    func saveImage(result: @escaping FlutterResult, call: FlutterMethodCall) {
        let args = call.arguments as? Dictionary<String, Any>
        let path = args![self.path] as! String
        
        let status = PHPhotoLibrary.authorizationStatus()
        if status == .notDetermined {
            PHPhotoLibrary.requestAuthorization({status in
                if status == .authorized{
                    self.performSavingImage(path: path, flutterResult: result)
                } else {
                    result(self.permissionDenied);
                }
            })
            
        } else if status == .authorized {
            self.performSavingImage(path: path, flutterResult: result)
        } else {
            result(self.pleaseGrantAccess);
        }
    }
    
    /// Saves image to the photos app
    ///
    /// - Parameters:
    ///   - path: path to temp file that needs to be stored in photos
    ///   - flutterResult: flutter result object that needs to be populated after
    /// operation finishes
    func performSavingImage(path: String, flutterResult: @escaping FlutterResult){
        
        let url = URL(fileURLWithPath: path)
        
        PHPhotoLibrary.shared().performChanges({
            PHAssetChangeRequest.creationRequestForAssetFromImage(atFileURL: url)
        }) { (success, error) in
            if success {
                flutterResult(self.imageSaved)
            } else {
                flutterResult(self.failedToSaveImage)
            }
        }
    }
    
    
    /// If user hasn't already permitted saving to the photos, it will be requested
    /// to do so.
    ///
    /// - Parameters:
    ///   - result: flutter result that gets sent back to the dart code
    ///   - call: method object with params for saving video
    func saveVideo(result: @escaping FlutterResult, call: FlutterMethodCall) {
        let args = call.arguments as? Dictionary<String, Any>
        let path = args![self.path] as! String
        
        let status = PHPhotoLibrary.authorizationStatus()
        if status == .notDetermined {
            PHPhotoLibrary.requestAuthorization({status in
                if status == .authorized{
                    self.performSavingVideo(flutterResult: result, path: path)
                } else {
                    result(self.permissionDenied);
                }
            })
        } else if status == .authorized {
            self.performSavingVideo( flutterResult: result, path: path)
        } else {
            result(self.pleaseGrantAccess);
        }
    }
    
    /// Saves video to the photos app
    ///
    /// - Parameters:
    ///   - path: path to temp file that needs to be stored in photos
    ///   - flutterResult: flutter result object that needs to be populated after
    /// operation finishes
    func performSavingVideo(flutterResult: @escaping FlutterResult, path: String){
        
        let url = URL(fileURLWithPath: path)
        
        PHPhotoLibrary.shared().performChanges({
            PHAssetChangeRequest.creationRequestForAssetFromVideo(atFileURL: url)
        }) { (success, error) in
            if success {
                flutterResult(self.videoSaved)
            } else {
                flutterResult(self.failedToSaveVideo)
            }
        }
    }
}

