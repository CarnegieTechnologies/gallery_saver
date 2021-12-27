import 'dart:async';
import 'dart:io';

import 'package:flutter/services.dart';
import 'package:gallery_saver/files.dart';
import 'package:http/http.dart' as http;
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';

class GallerySaver {
  static late var _debug = false;
  static const String _channelName = 'gallery_saver';
  static const String _methodSaveImage = 'saveImage';
  static const String _methodSaveVideo = 'saveVideo';

  static const String _pleaseProvidePath = 'Please provide valid file path.';
  static const String _fileIsNotVideo = 'File on path is not a video.';
  static const String _fileIsNotImage = 'File on path is not an image.';
  static const MethodChannel _channel = const MethodChannel(_channelName);

  /// set the debug value, print information only when in debug mode.
  static setDebug({required bool debug}) {
    _debug = debug;
  }

  ///saves video from provided temp path and optional album name in gallery
  static Future<bool?> saveVideo(
    String path, {
    String? albumName,
    bool toDcim = false,
    Map<String, String>? headers,
  }) async {
    File? tempFile;
    if (path.isEmpty) {
      throw ArgumentError(_pleaseProvidePath);
    }
    if (!isVideo(path)) {
      throw ArgumentError(_fileIsNotVideo);
    }
    if (!isLocalFilePath(path)) {
      tempFile = await _downloadFile(path, headers: headers);
      path = tempFile.path;
    }
    bool? result = await _channel.invokeMethod(
      _methodSaveVideo,
      <String, dynamic>{'path': path, 'albumName': albumName, 'toDcim': toDcim},
    );
    if (tempFile != null) {
      tempFile.delete();
    }
    return result;
  }

  ///saves image from provided temp path and optional album name in gallery
  static Future<bool?> saveImage(
    String path, {
    String? albumName,
    bool toDcim = false,
    Map<String, String>? headers,
  }) async {
    File? tempFile;
    if (path.isEmpty) {
      throw ArgumentError(_pleaseProvidePath);
    }
    if (!isImage(path)) {
      throw ArgumentError(_fileIsNotImage);
    }
    if (!isLocalFilePath(path)) {
      tempFile = await _downloadFile(path, headers: headers);
      path = tempFile.path;
    }

    bool? result = await _channel.invokeMethod(
      _methodSaveImage,
      <String, dynamic>{'path': path, 'albumName': albumName, 'toDcim': toDcim},
    );
    if (tempFile != null) {
      tempFile.delete();
    }

    return result;
  }

  static Future<File> _downloadFile(
    String url, {
    Map<String, String>? headers,
  }) async {
    if (_debug) {
      print(url);
      print(headers);
    }
    http.Client _client = new http.Client();
    var req = await _client.get(Uri.parse(url), headers: headers);
    if (req.statusCode >= 400) {
      throw HttpException(req.statusCode.toString());
    }
    var bytes = req.bodyBytes;
    String dir = (await getTemporaryDirectory()).path;
    File file = new File('$dir/${basename(url)}');
    await file.writeAsBytes(bytes);
    if (_debug) {
      print('File size:${await file.length()}');
      print(file.path);
    }
    return file;
  }
}
