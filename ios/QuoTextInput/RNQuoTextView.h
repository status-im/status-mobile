//
//  RNQuoTextView.h
//  StatusIm
//
//  Created by Gheorghe on 12.11.2020.
//  Copyright © 2020 Status. All rights reserved.
//

#ifndef RNQuoTextView_h
#define RNQuoTextView_h

#if __has_include(<RCTText/RCTBaseTextInputView.h>)
#import <RCTText/RCTBaseTextInputView.h>
#else
#import "RCTBaseTextInputView.h"
#endif

NS_ASSUME_NONNULL_BEGIN

@interface RNQuoTextView : RCTBaseTextInputView

@property (nullable, nonatomic, copy) NSArray<NSString *> *menuItems;
@property (nonatomic, copy) RCTDirectEventBlock onItemPress;

@end

NS_ASSUME_NONNULL_END

#endif /* RNQuoTextView_h */
