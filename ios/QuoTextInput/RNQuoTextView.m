//
//  RNQuoTextView.m
//  StatusIm
//
//  Created by Gheorghe on 12.11.2020.
//  Copyright © 2020 Status. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "RNQuoTextView.h"

#if __has_include(<RCTText/RCTTextSelection.h>)
#import <RCTText/RCTTextSelection.h>
#else
#import "RCTTextSelection.h"
#endif

#if __has_include(<RCTText/RCTUITextView.h>)
#import <RCTText/RCTUITextView.h>
#else
#import "RCTUITextView.h"
#endif

#if __has_include(<RCTText/RCTTextAttributes.h>)
#import <RCTText/RCTTextAttributes.h>
#else
#import "RCTTextAttributes.h"
#endif

#import <React/RCTUtils.h>

@implementation RNQuoTextView
{
    RCTUITextView *_backedTextInputView;
}

UITextPosition *selectionStart;
UITextPosition* beginning;

- (instancetype)initWithBridge:(RCTBridge *)bridge
{
  if (self = [super initWithBridge:bridge]) {
    self.blurOnSubmit = NO;

    _backedTextInputView = [[RCTUITextView alloc] initWithFrame:self.bounds];
    _backedTextInputView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    _backedTextInputView.textInputDelegate = self;

  
    UITapGestureRecognizer *tapGesture = [ [UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTap:)];
    tapGesture.numberOfTapsRequired = 2;
    

            [_backedTextInputView addGestureRecognizer:tapGesture];
         
    [self addSubview:_backedTextInputView];
  }

  return self;
}

- (id<RCTBackedTextInputViewProtocol>)backedTextInputView
{
  return _backedTextInputView;
}

#pragma mark - UIScrollViewDelegate

- (void)scrollViewDidScroll:(UIScrollView *)scrollView
{
  RCTDirectEventBlock onScroll = self.onScroll;

  if (onScroll) {
    CGPoint contentOffset = scrollView.contentOffset;
    CGSize contentSize = scrollView.contentSize;
    CGSize size = scrollView.bounds.size;
    UIEdgeInsets contentInset = scrollView.contentInset;

    onScroll(@{
      @"contentOffset": @{
        @"x": @(contentOffset.x),
        @"y": @(contentOffset.y)
      },
      @"contentInset": @{
        @"top": @(contentInset.top),
        @"left": @(contentInset.left),
        @"bottom": @(contentInset.bottom),
        @"right": @(contentInset.right)
      },
      @"contentSize": @{
        @"width": @(contentSize.width),
        @"height": @(contentSize.height)
      },
      @"layoutMeasurement": @{
        @"width": @(size.width),
        @"height": @(size.height)
      },
      @"zoomScale": @(scrollView.zoomScale ?: 1),
    });
  }
}

NSString *const CUSTOM_SELECTOR = @"_CUSTOM_SELECTOR_";

- (void)tappedMenuItem:(NSString *)eventType
{
    RCTTextSelection *selection = self.selection;
    
    NSUInteger start = selection.start;
    NSUInteger end = selection.end - selection.start;
    
    self.onItemPress(@{
        @"content": [[self.attributedText string] substringWithRange:NSMakeRange(start, end)],
        @"eventType": eventType,
        @"selectionStart": @(start),
        @"selectionEnd": @(selection.end)
    });
    
    [_backedTextInputView setSelectedTextRange:nil notifyDelegate:false];
}

-(void) _handleGesture
{
    if (!_backedTextInputView.isFirstResponder) {
        [_backedTextInputView becomeFirstResponder];
    }
    
    UIMenuController *menuController = [UIMenuController sharedMenuController];
    
    if (menuController.isMenuVisible) return;
    
    NSMutableArray *menuControllerItems = [NSMutableArray arrayWithCapacity:self.menuItems.count];
    
    for(NSDictionary *menuItem in self.menuItems) {
      NSString *sel = [NSString stringWithFormat:@"%@%@", CUSTOM_SELECTOR, menuItem[@"type"]];
      UIMenuItem *item = [[UIMenuItem alloc] initWithTitle: menuItem[@"title"]
                                                      action: NSSelectorFromString(sel)];
        
        [menuControllerItems addObject: item];
    }
    menuController.menuItems = menuControllerItems;
    [menuController setTargetRect:self.bounds inView:self];
    [menuController setMenuVisible:YES animated:YES];
}


- (NSMethodSignature *)methodSignatureForSelector:(SEL)sel
{
    if ([super methodSignatureForSelector:sel]) {
        return [super methodSignatureForSelector:sel];
    }
    return [super methodSignatureForSelector:@selector(tappedMenuItem:)];
}

- (void)forwardInvocation:(NSInvocation *)invocation
{
    NSString *sel = NSStringFromSelector([invocation selector]);
    NSRange match = [sel rangeOfString:CUSTOM_SELECTOR];
    if (match.location == 0) {
        [self tappedMenuItem:[sel substringFromIndex:17]];
    } else {
        [super forwardInvocation:invocation];
    }
}

-(void) handleTap: (UITapGestureRecognizer *) gesture
{
    [_backedTextInputView select:self];
    [_backedTextInputView selectAll:self];
    [self _handleGesture];
}


- (BOOL)canBecomeFirstResponder
{
    return YES;
}

- (BOOL)canPerformAction:(SEL)action withSender:(id)sender
{
  NSString *sel = NSStringFromSelector(action);
  NSRange match = [sel rangeOfString:CUSTOM_SELECTOR];
  NSString * pasteImage = [NSString stringWithFormat:@"%@%@", CUSTOM_SELECTOR, @"PasteImage"];
  UIPasteboard *pasteboard = [UIPasteboard generalPasteboard];
  bool imagePaste = pasteboard.hasImages;
  
  if (action == @selector(paste:) && imagePaste) {
    return NO;
  }
 
  if (sel == pasteImage && !imagePaste) {
    return NO;
  }
  
  if (match.location == 0) {
    return YES;
  }
  
  return [super canPerformAction:action withSender:sender];
}


@end
