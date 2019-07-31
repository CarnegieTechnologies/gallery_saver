#import "GallerySaverPlugin.h"
#import <gallery_saver/gallery_saver-Swift.h>

@implementation GallerySaverPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftGallerySaverPlugin registerWithRegistrar:registrar];
}
@end
