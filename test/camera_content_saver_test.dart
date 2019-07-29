import 'package:camera_content_saver/camera_content_saver.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  const MethodChannel channel = MethodChannel('camera_content_saver');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      switch (methodCall.method) {
        case 'saveImage':
          return 'image saved';
        case 'saveVideo':
          return 'video saved';
      }

      return 'unknown method';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('save image', () async {
    expect(await CameraContentSaver.saveImage('/storage/emulated/...'),
        'image saved');
  });

  test('save video', () async {
    expect(await CameraContentSaver.saveVideo('/storage/emulated/...'),
        'video saved');
  });
}
