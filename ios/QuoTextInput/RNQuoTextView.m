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

- (instancetype)initWithBridge:(RCTBridge *)bridge
{
  if (self = [super initWithBridge:bridge]) {
    self.blurOnSubmit = NO;
    
    _backedTextInputView = [[RCTUITextView alloc] initWithFrame:self.bounds];
    _backedTextInputView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;
    _backedTextInputView.textInputDelegate = self;
    _backedTextInputView.selectable = YES;
    
    [self addSubview:_backedTextInputView];
    
    UITapGestureRecognizer *tapGesture = [ [UITapGestureRecognizer alloc] initWithTarget:self action:@selector(handleTap:)];
    tapGesture.numberOfTapsRequired = 2;
    
    [_backedTextInputView addGestureRecognizer:tapGesture];
  }
  
  return self;
}

- (id<RCTBackedTextInputViewProtocol>)backedTextInputView
{
  return _backedTextInputView;
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
    if(![menuItem[@"type"] isEqualToString:@"Paste"]){
      NSString *sel = [NSString stringWithFormat:@"%@%@", CUSTOM_SELECTOR, menuItem[@"type"]];
      UIMenuItem *item = [[UIMenuItem alloc] initWithTitle: menuItem[@"title"]
                                                    action: NSSelectorFromString(sel)];
      
      [menuControllerItems addObject: item];
    }
    
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
  
  if ([invocation selector] == @selector(paste:)) {
    [self tappedMenuItem:@"Paste"];
  }else if (match.location == 0) {
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

#pragma mark - Context Menu

- (BOOL)canPerformAction:(SEL)action withSender:(id)sender
{
  NSString *sel = NSStringFromSelector(action);
  NSRange match = [sel rangeOfString:CUSTOM_SELECTOR];
  
  if (match.location == 0) {
    return YES;
  }
  
  if (action == @selector(paste:)) {
    return YES;
  }
  
  return [super canPerformAction:action withSender:sender];
  
}

@end
