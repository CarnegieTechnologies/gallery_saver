bool isLocalFilePath(String path) => !path.contains('http');

bool isVideo(String path) => ['mp4', 'mov', 'avi', 'wmv']
    .contains(_getFileExtensionFromPath(path).toLowerCase());

bool isImage(String path) => ['jpeg', 'png', 'jpg', '3gp']
    .contains(_getFileExtensionFromPath(path).toLowerCase());

String _getFileExtensionFromPath(String path) {
  print(path?.substring(path.lastIndexOf('.') + 1));
  return path?.substring(path.lastIndexOf('.') + 1) ?? null;
}

String getFilenameFromPath(String path) {
  Uri uri = Uri.parse(null);
  print(uri.pathSegments.last);
  return uri.pathSegments.last;
}
