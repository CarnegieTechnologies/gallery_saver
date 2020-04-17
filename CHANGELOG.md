## 2.0.1

* Reverted PR for image validation 

## 2.0.0 

* Merged all PRs(image validation to native, error if image was selected twice, newest android and ios support)

## 1.0.7 

* Fixed issue with improper mime types for video

## 1.0.6

* Ios save image to photos crash fix.

## 1.0.5

* Support saving images in separate folder in gallery
* Android:
* By default image will be saved at "pictures" system folder,
* and video at "movies" system folder.If user set folder name it will be
* at root external storage.
* iOS:
* By default image and video will be saved at photos.
* If user set folder name it will be added as new album at photos.

## 1.0.4

* Fixed bug with mime type on Android 10

## 1.0.3

* Remove deleting temp video after it gets saved into gallery

## 1.0.2

* Saving large video files - fix

## 1.0.1

* Changed description

## 1.0.0

* Support saving network images and videos to gallery

## 0.0.5

* Fix for colliding permission request with image_picker plugin

## 0.0.4

* Return type changed to bool(true for success and false for everything else)

## 0.0.3

* Fixed crash when requesting storage access on Android.

## 0.0.2

* Added swift version and changed description.

## 0.0.1

* Initial release. Image and video from provided temp path get saved to device(Gallery and Photos).