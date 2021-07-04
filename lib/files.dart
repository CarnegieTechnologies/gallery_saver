const List<String> videoFormats = [
  'mp4',
  'mov',
  'avi',
  'wmv',
  '3gp',
  'mkv',
  'flv'
];
const List<String> imageFormats = [
  'jpeg',
  'png',
  'jpg',
  'gif',
  'webp',
  'tif',
  'heic'
];
const http = 'http';

bool isLocalFilePath(String path) {
  Uri uri = Uri.parse(path);
  return !uri.scheme.contains(http);
}

bool isVideo(String path) {
  List<String> parsedSegment = Uri.parse(path).pathSegments.last.split(".");
  return parsedSegment.length == 1
      ? false
      : videoFormats.contains(parsedSegment.last.toLowerCase());
}

bool isImage(String path) {
  List<String> parsedSegment = Uri.parse(path).pathSegments.last.split(".");
  return parsedSegment.length == 1
      ? false
      : imageFormats.contains(parsedSegment.last.toLowerCase());
}
