import 'dart:async';

import 'package:flutter/services.dart';

class GallerySaver {
  static const MethodChannel _channel =
      const MethodChannel('gallery_saver');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
