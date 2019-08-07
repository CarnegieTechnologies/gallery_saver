const List<String> videoFormats = ['mp4', 'mov', 'avi', 'wmv'];
const List<String> imageFormats = ['jpeg', 'png', 'jpg', '3gp'];

bool isLocalFilePath(String path) {
  Uri uri = Uri.parse(path);
  return !uri.scheme.contains('http');
}

bool isVideo(String path) =>
    videoFormats.contains(_getFileExtensionFromPath(path).toLowerCase());

bool isImage(String path) =>
    imageFormats.contains(_getFileExtensionFromPath(path).toLowerCase());

String _getFileExtensionFromPath(String path) {
  print(path?.substring(path.lastIndexOf('.') + 1));
  return path?.substring(path.lastIndexOf('.') + 1) ?? null;
}

String getFilenameFromPath(String path) {
  Uri uri = Uri.parse(path);
  print(uri.pathSegments.last);
  return uri.pathSegments.last;
}
