import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:camera_content_saver/camera_content_saver.dart';

void main() {
  const MethodChannel channel = MethodChannel('camera_content_saver');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await CameraContentSaver.platformVersion, '42');
  });
}
