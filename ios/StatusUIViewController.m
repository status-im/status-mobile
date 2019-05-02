//
//  StatusUIViewController.m
//  StatusIm
//
//  Created by Roman Volosovskyi on 5/2/19.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "StatusUIViewController.h"

@implementation StatusUIViewController

- (void)mailComposeController:(MFMailComposeViewController *)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError *)error
{
  switch (result) {
    case MFMailComposeResultSent:
      NSLog(@"You sent the email.");
      break;
    case MFMailComposeResultSaved:
      NSLog(@"You saved a draft of this email");
      break;
    case MFMailComposeResultCancelled:
      NSLog(@"You cancelled sending this email.");
      break;
    case MFMailComposeResultFailed:
      NSLog(@"Mail failed:  An error occurred when trying to compose this email");
      break;
    default:
      NSLog(@"An error occurred when trying to compose this email");
      break;
  }
  
  [self dismissViewControllerAnimated:YES completion:NULL];
}

@end
