import 'dart:async';

import 'package:flutter/services.dart';

class CameraContentSaver {
  static const String channelName = 'camera_content_saver';
  static const String methodSaveImage = 'saveImage';
  static const String methodSaveVideo = 'saveVideo';

  static const MethodChannel _channel = const MethodChannel(channelName);

  static Future<String> saveVideo(String path) async {
    String filePath = await _channel.invokeMethod(
      methodSaveVideo,
      <String, dynamic>{'path': path},
    );

    //process ios return filePath
    if (filePath.startsWith("file://")) {
      filePath = filePath.replaceAll("file://", "");
    }
    return filePath;
  }

  static Future<String> saveImage(String path) async {
    String filePath = await _channel.invokeMethod(
      methodSaveImage,
      <String, dynamic>{'path': path},
    );

    //process ios return filePath
    if (filePath.startsWith("file://")) {
      filePath = filePath.replaceAll("file://", "");
    }
    return filePath;
  }
}
