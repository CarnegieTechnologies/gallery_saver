#import "GallerySaverPlugin.h"

#import <Photos/Photos.h>

typedef NS_ENUM (NSUInteger, MediaType) {
    MediaTypeImage,
    MediaTypeVideo
};

@implementation GallerySaverPlugin

+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
    FlutterMethodChannel* channel = [FlutterMethodChannel
                                     methodChannelWithName:@"gallery_saver"
                                     binaryMessenger:[registrar messenger]];
    GallerySaverPlugin* instance = [[GallerySaverPlugin alloc] init];
    [registrar addMethodCallDelegate:instance channel:channel];
}

- (void)handleMethodCall:(FlutterMethodCall*)call result:(FlutterResult)result {
    
    if ([@"saveImage" isEqualToString:call.method]) {
        [self saveMedia:call mediaType:MediaTypeImage result:result];
    }else if ([@"saveVideo" isEqualToString:call.method]) {
        [self saveMedia:call mediaType:MediaTypeVideo result:result];
    }else {
        result(FlutterMethodNotImplemented);
    }
    
}
- (void)saveMedia:(FlutterMethodCall*)call mediaType:(MediaType)mediaType result:(FlutterResult)result {
    
    NSDictionary * args = call.arguments;
    
    if (![args isKindOfClass:[NSDictionary class]]) {
        result(false);
        return;
    }
    
    NSString * path = args[@"path"];
    NSString * albumName = args[@"albumName"];
    
    PHAuthorizationStatus status = [PHPhotoLibrary authorizationStatus];
    
    if (status == PHAuthorizationStatusNotDetermined) {
        [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
            if (status == PHAuthorizationStatusAuthorized) {
                [self _saveMediaToAlbum:path mediaType:mediaType albumName:albumName result:result];
            }else {
                result(@NO);
            }
        }];
    }else if (status == PHAuthorizationStatusAuthorized){
        [self _saveMediaToAlbum:path mediaType:mediaType albumName:albumName result:result];
    }else {
        result(@NO);
    }
}

- (void)_saveMediaToAlbum:(NSString *)imagePath mediaType:(MediaType)mediaType albumName:(NSString *)albumName result:(FlutterResult)flutterResult {
    
    if (!albumName) {
        [self saveFile:imagePath mediaType:mediaType album:nil result:flutterResult];
    }else {
        PHAssetCollection * album = [self fetchAssetCollectionForAlbum:albumName];
        
        if (album) {
            [self saveFile:imagePath mediaType:mediaType album:album result:flutterResult];
        }else {
            
            [self createAppPhotosAlbum:albumName completion:^(NSError * _Nullable error) {
                
                if (error) {
                    flutterResult(@NO);
                    return;
                }else {
                    PHAssetCollection * album = [self fetchAssetCollectionForAlbum:albumName];
                    if (album) {
                        [self saveFile:imagePath mediaType:mediaType album:album result:flutterResult];
                    }else {
                        flutterResult(@NO);
                    }
                }
            }];
        }
    }
    
}

- (void)saveFile:(NSString *)path mediaType:(MediaType)mediaType album:(PHAssetCollection *)album result:(FlutterResult)flutterResult {
    
    NSURL * url =[NSURL fileURLWithPath:path];
    
    [[PHPhotoLibrary sharedPhotoLibrary] performChanges:^{
        
        PHAssetChangeRequest * assetCreationRequest;
        
        if (mediaType == MediaTypeImage) {
            assetCreationRequest = [PHAssetChangeRequest creationRequestForAssetFromImageAtFileURL:url];
        }else {
            assetCreationRequest = [PHAssetChangeRequest creationRequestForAssetFromVideoAtFileURL:url];
        }
        
        if (album) {
            
            PHAssetCollectionChangeRequest * assetCollectionChangeRequest = [PHAssetCollectionChangeRequest changeRequestForAssetCollection:album];
            
            PHObjectPlaceholder * createdAssetPlaceholder =  [assetCreationRequest placeholderForCreatedAsset];
            
            if (!createdAssetPlaceholder) {
                return;
            }
            [assetCollectionChangeRequest addAssets:@[createdAssetPlaceholder]];
            
        }
        
    } completionHandler:^(BOOL success, NSError * _Nullable error) {
        if (success) {
            flutterResult(@YES);
        }else {
            flutterResult(@NO);
        }
    }];
    
}

- (PHAssetCollection *)fetchAssetCollectionForAlbum:(NSString *) albumName{
    
    PHFetchOptions * fetchOptions = [[PHFetchOptions alloc]init];
    fetchOptions.predicate = [NSPredicate predicateWithFormat:[NSString stringWithFormat:@"title = \"%@\"",albumName]];
    
    PHFetchResult<PHAssetCollection *> * collection = [PHAssetCollection fetchAssetCollectionsWithType:PHAssetCollectionTypeAlbum subtype:PHAssetCollectionSubtypeAny options:fetchOptions];
    
    if (collection && collection.count > 0) {
        return collection.firstObject;
    }
    return nil;
}

- (void)createAppPhotosAlbum:(NSString *)albumName completion:(void(^)(NSError * _Nullable error))completion {
    
    [[PHPhotoLibrary sharedPhotoLibrary] performChanges:^{
        
        [PHAssetCollectionChangeRequest creationRequestForAssetCollectionWithTitle:albumName];
        
    } completionHandler:^(BOOL success, NSError * _Nullable error) {
        
        dispatch_async(dispatch_get_main_queue(), ^{
            
            if (completion){
                completion(error);
            }
        });
    }];
    
}

@end
