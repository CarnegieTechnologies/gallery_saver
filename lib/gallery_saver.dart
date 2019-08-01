import 'dart:async';

import 'package:flutter/services.dart';

class GallerySaver {
  static const String channelName = 'gallery_saver';
  static const String methodSaveImage = 'saveImage';
  static const String methodSaveVideo = 'saveVideo';

  static const String pleaseProvidePath = 'Please provide valid file path';

  static const MethodChannel _channel = const MethodChannel(channelName);

  ///saves video from provided temp path
  static Future<String> saveVideo(String path) async {
    if (path == null || path.isEmpty) {
      throw ArgumentError(pleaseProvidePath);
    }

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

  ///saves image from provided temp path
  static Future<String> saveImage(String path) async {
    if (path == null || path.isEmpty) {
      throw ArgumentError(pleaseProvidePath);
    }

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