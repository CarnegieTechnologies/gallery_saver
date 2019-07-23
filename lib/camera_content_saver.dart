import 'dart:async';

import 'package:flutter/services.dart';

class CameraContentSaver {
  static const MethodChannel _channel =
      const MethodChannel('camera_content_saver');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
