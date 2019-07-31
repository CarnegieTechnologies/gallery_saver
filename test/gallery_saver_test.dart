import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gallery_saver/gallery_saver.dart';

void main() {
  const MethodChannel channel = MethodChannel('gallery_saver');

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await GallerySaver.platformVersion, '42');
  });
}
