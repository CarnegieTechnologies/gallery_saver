const List<String> formats = [
  'mp4',
  'mov',
  'avi',
  'wmv',
  '3gp',
  'mkv',
  'flv',
  'jpeg',
  'png',
  'jpg',
  'gif',
  'webp',
  'tif',
  'heic',
];

const http = 'http';

bool isLocalFilePath(String path) {
  Uri uri = Uri.parse(path);
  return !uri.scheme.contains(http);
}

bool isValidPath(String path) {
  if (path.isEmpty) return false;
  List<String> parsedSegment = Uri.parse(path).pathSegments.last.split(".");
  return parsedSegment.length == 1
      ? false
      : formats.contains(parsedSegment.last.toLowerCase());
}
