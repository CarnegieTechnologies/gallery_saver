import 'package:path/path.dart';

const List<String> videoFormats = [
  '.mp4',
  '.mov',
  '.avi',
  '.wmv',
  '.3gp',
  '.mkv',
  '.flv'
];

const http = 'http';

bool isLocalFilePath(String path) {
  Uri uri = Uri.parse(path);
  return !uri.scheme.contains(http);
}

bool isVideo(String path) =>
    videoFormats.contains(extension(path).toLowerCase());
