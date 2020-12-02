//
//  RNQuoTextViewManager.m
//  StatusIm
//
//  Created by Gheorghe on 12.11.2020.
//  Copyright © 2020 Status. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RNQuoTextViewManager.h"
#import "RNQuoTextView.h"

@implementation RNQuoTextViewManager

RCT_EXPORT_MODULE(RNQuoTextView)

- (UIView *)view
{
    RNQuoTextView *textInput = [[RNQuoTextView alloc] initWithBridge:self.bridge];
    return textInput;
}

RCT_EXPORT_VIEW_PROPERTY(menuItems, NSArray);
RCT_EXPORT_VIEW_PROPERTY(onItemPress, RCTDirectEventBlock);

@end
