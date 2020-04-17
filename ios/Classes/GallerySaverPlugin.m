#import "GallerySaverPlugin.h"

#if __has_include(<gallery_saver/gallery_saver-Swift.h>)
#import <gallery_saver/gallery_saver-Swift.h>
#else
#import "gallery_saver-Swift.h"
#endif


@implementation GallerySaverPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftGallerySaverPlugin registerWithRegistrar:registrar];
}
@end
