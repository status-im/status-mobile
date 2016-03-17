/**
 * Copyright (c) 2015-present, Facebook, Inc.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */

#import <UIKit/UIKit.h>
#import <Geth/Geth.h>
#import "AppDelegate.h"

int main(int argc, char * argv[]) {
  @autoreleasepool {
    dispatch_async(dispatch_get_global_queue( DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
      run("--bootnodes enode://e2f28126720452aa82f7d3083e49e6b3945502cb94d9750a15e27ee310eed6991618199f878e5fbc7dfa0e20f0af9554b41f491dc8f1dbae8f0f2d37a3a613aa@139.162.13.89:30303 --shh --ipcdisable --nodiscover --rpc --rpcapi db,eth,net,web3,shh --fast");
    });
    
    return UIApplicationMain(argc, argv, nil, NSStringFromClass([AppDelegate class]));
  }
}
