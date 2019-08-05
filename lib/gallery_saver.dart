import 'dart:async';

import 'package:flutter/services.dart';

class GallerySaver {
  static const String channelName = 'gallery_saver';
  static const String methodSaveImage = 'saveImage';
  static const String methodSaveVideo = 'saveVideo';

  static const String pleaseProvidePath = 'Please provide valid file path';

  static const MethodChannel _channel = const MethodChannel(channelName);

  ///saves video from provided temp path
  static Future<bool> saveVideo(String path) async {
    if (path == null || path.isEmpty) {
      throw ArgumentError(pleaseProvidePath);
    }

    bool result = await _channel.invokeMethod(
      methodSaveVideo,
      <String, dynamic>{'path': path},
    );

    return result;
  }

  ///saves image from provided temp path
  static Future<bool> saveImage(String path) async {
    if (path == null || path.isEmpty) {
      throw ArgumentError(pleaseProvidePath);
    }

    bool result = await _channel.invokeMethod(
      methodSaveImage,
      <String, dynamic>{'path': path},
    );

    return result;
  }
}
