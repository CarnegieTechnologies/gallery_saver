import 'dart:async';
import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/services.dart';
import 'package:gallery_saver/files.dart';
import 'package:http/http.dart' as http;
import 'package:path/path.dart';

class GallerySaver {
  static const String channelName = 'gallery_saver';
  static const String methodSaveImage = 'saveImage';
  static const String methodSaveVideo = 'saveVideo';

  static const String pleaseProvidePath = 'Please provide valid file path.';
  static const String fileIsNotVideo = 'File on path is not a video.';
  static const String fileIsNotImage = 'File on path is not an image.';
  static const MethodChannel _channel = const MethodChannel(channelName);

  ///saves video from provided temp path and optional album name in gallery
  static Future<bool?> saveVideo(
    String path, {
    String? albumName,
    bool toDcim = false,
    Map<String, String>? headers,
  }) async {
    if (path.isEmpty) {
      throw ArgumentError(pleaseProvidePath);
    }
    if (!isVideo(path)) {
      throw ArgumentError(fileIsNotVideo);
    }
    final Uint8List bytes = await (isLocalFilePath(path) ? File(path).readAsBytes() : http.readBytes(Uri.parse(path)));
    bool? result = await _channel.invokeMethod(
      methodSaveVideo,
      <String, dynamic>{'bytes': bytes, 'fileName': basename(path), 'albumName': albumName, 'toDcim': toDcim},
    );
    return result;
  }

  ///saves image from provided temp path and optional album name in gallery
  static Future<bool?> saveImage(
    String path, {
    String? albumName,
    bool toDcim = false,
    Map<String, String>? headers,
  }) async {
    if (path.isEmpty) {
      throw ArgumentError(pleaseProvidePath);
    }
    if (!isImage(path)) {
      throw ArgumentError(fileIsNotImage);
    }
    final Uint8List bytes = await (isLocalFilePath(path) ? File(path).readAsBytes() : http.readBytes(Uri.parse(path)));
    bool? result = await _channel.invokeMethod(
      methodSaveImage,
      <String, dynamic>{'bytes': bytes, 'fileName': basename(path), 'albumName': albumName, 'toDcim': toDcim},
    );
    return result;
  }
}
