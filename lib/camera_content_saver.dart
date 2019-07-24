import 'dart:async';
import 'dart:typed_data';

import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class CameraContentSaver {
  static const String channelName = 'camera_content_saver';
  static const String methodPlatformVersion = 'getPlatformVersion';
  static const String methodSaveImage = 'saveImage';
  static const String methodSaveVideo = 'saveVideo';

  static const MethodChannel _channel = const MethodChannel(channelName);

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod(methodPlatformVersion);
    return version;
  }

//save video
  static Future<String> saveVideo(Uint8List fileData,
      {String title, String description}) async {
    assert(fileData != null);

    //save image and return its path as string
    String filePath = await _channel.invokeMethod(
      methodSaveVideo,
      <String, dynamic>{
        'fileData': fileData,
        'title': title,
        'description': description
      },
    );
    debugPrint("saved filePath:" + filePath);
    //process ios return filePath
    if (filePath.startsWith("file://")) {
      filePath = filePath.replaceAll("file://", "");
    }
    return filePath;
  }

//save image
  static Future<String> saveImage(Uint8List fileData, String path,
      {String title, String description}) async {
    assert(fileData != null);

    //save image and return its path as string
    String filePath = await _channel.invokeMethod(
      methodSaveImage,
      <String, dynamic>{
        'fileData': fileData,
        'path': path,
        'title': title,
        'description': description
      },
    );
    debugPrint("saved filePath:" + filePath);
    //process ios return filePath
    if (filePath.startsWith("file://")) {
      filePath = filePath.replaceAll("file://", "");
    }
    return filePath;
  }
}
