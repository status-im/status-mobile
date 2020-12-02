//
//  RNQuoTextViewManager.h
//  StatusIm
//
//  Created by Gheorghe on 12.11.2020.
//  Copyright © 2020 Status. All rights reserved.
//

#ifndef RNQuoTextViewManager_h
#define RNQuoTextViewManager_h

#if __has_include(<RCTText/RCTBaseTextInputViewManager.h>)
#import <RCTText/RCTBaseTextInputViewManager.h>
#else
#import "RCTBaseTextInputViewManager.h"
#endif

NS_ASSUME_NONNULL_BEGIN

@interface RNQuoTextViewManager : RCTBaseTextInputViewManager

@property (nullable, nonatomic, copy) NSArray<NSString *> *menuItems;
@property (nonatomic, copy) RCTDirectEventBlock onItemPress;

@end

NS_ASSUME_NONNULL_END


#endif /* RNQuoTextViewManager_h */
