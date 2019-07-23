#import "CameraContentSaverPlugin.h"
#import <camera_content_saver/camera_content_saver-Swift.h>

@implementation CameraContentSaverPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftCameraContentSaverPlugin registerWithRegistrar:registrar];
}
@end
