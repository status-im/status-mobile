//
//  ClipboardReactModuleExports.m
//  StatusIm
//
//  Created by Gheorghe on 03.11.2020.
//  Copyright © 2020 Status. All rights reserved.
//

#import "ClipboardReactModuleExports.h"
#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(MediaClipboard, NSObject)

RCT_EXTERN_METHOD(copyImage:(NSString *)base64Image
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(paste:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)
RCT_EXTERN_METHOD(hasImages:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject)

@end
