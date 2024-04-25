#import <MessageUI/MessageUI.h>
#import "MailManager.h"
#import <React/RCTConvert.h>
#import <React/RCTLog.h>
#import "React/RCTBridge.h"
#import "React/RCTEventDispatcher.h"

@implementation MailManager
{
    NSMutableDictionary *_callbacks;
}

- (instancetype)init
{
    if ((self = [super init])) {
        _callbacks = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

RCT_EXPORT_MODULE();

RCT_EXPORT_METHOD(mail:(NSDictionary *)options
                  callback: (RCTResponseSenderBlock)callback)
{
    if ([MFMailComposeViewController canSendMail])
    {
        MFMailComposeViewController *mail = [[MFMailComposeViewController alloc] init];
        mail.mailComposeDelegate = self;
        _callbacks[RCTKeyForInstance(mail)] = callback;

        if (options[@"subject"]){
            NSString *subject = [RCTConvert NSString:options[@"subject"]];
            [mail setSubject:subject];
        }

        BOOL isHTML = NO;

        if (options[@"isHTML"]){
            isHTML = [options[@"isHTML"] boolValue];
        }

        if (options[@"body"]){
            NSString *body = [RCTConvert NSString:options[@"body"]];
            [mail setMessageBody:body isHTML:isHTML];
        }

        if (options[@"recipients"]){
            NSArray *recipients = [RCTConvert NSArray:options[@"recipients"]];
            [mail setToRecipients:recipients];
        }

        if (options[@"ccRecipients"]){
            NSArray *ccRecipients = [RCTConvert NSArray:options[@"ccRecipients"]];
            [mail setCcRecipients:ccRecipients];
        }

        if (options[@"bccRecipients"]){
            NSArray *bccRecipients = [RCTConvert NSArray:options[@"bccRecipients"]];
            [mail setBccRecipients:bccRecipients];
        }
        if (options[@"attachments"]) {
            NSArray *attachments = [RCTConvert NSArray:options[@"attachments"]];
            for (NSDictionary *attachment in attachments) {
                if ((attachment[@"path"] || attachment[@"uri"]) && (attachment[@"type"] || attachment[@"mimeType"])) {
                    NSString *attachmentPath = [RCTConvert NSString:attachment[@"path"]];
                    NSString *attachmentUri = [RCTConvert NSString:attachment[@"uri"]];
                    NSString *attachmentType = [RCTConvert NSString:attachment[@"type"]];
                    NSString *attachmentName = [RCTConvert NSString:attachment[@"name"]];
                    NSString *attachmentMimeType = [RCTConvert NSString:attachment[@"mimeType"]];

                    // Set default filename if not specificed
                    if (!attachmentName) {
                        attachmentName = [[attachmentPath lastPathComponent] stringByDeletingPathExtension];
                    }

                    NSData *fileData;
                    if (attachmentPath) {
                        NSFileManager *fileManager = [NSFileManager defaultManager];
                        if (![fileManager fileExistsAtPath:attachmentPath]){
                            callback(@[[NSString stringWithFormat: @"attachment file with path '%@' does not exist", attachmentPath]]);
                            return;
                        }
                        // Get the resource path and read the file using NSData
                        fileData = [NSData dataWithContentsOfFile:attachmentPath];
                    } else if (attachmentUri) {
                        // Get the URI and read it using NSData
                        NSURL *attachmentURL = [[NSURLComponents componentsWithString:attachmentUri] URL];
                        NSError *error = nil;
                        fileData = [NSData dataWithContentsOfURL:attachmentURL options:0 error:&error];
                        if (!fileData) {
                            callback(@[[NSString stringWithFormat: @"attachment file with uri '%@' does not exist", attachmentUri]]);
                            return;
                        }
                    }

                    // Determine the MIME type
                    NSString *mimeType;
                    if (attachmentType) {
                        /*
                         * Add additional mime types and PR if necessary. Find the list
                         * of supported formats at http://www.iana.org/assignments/media-types/media-types.xhtml
                         */
                        NSDictionary *supportedMimeTypes = @{
                            @"jpeg" : @"image/jpeg",
                            @"jpg" : @"image/jpeg",
                            @"png" : @"image/png",
                            @"doc" : @"application/msword",
                            @"docx" : @"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                            @"ppt" : @"application/vnd.ms-powerpoint",
                            @"pptx" : @"application/vnd.openxmlformats-officedocument.presentationml.presentation",
                            @"html" : @"text/html",
                            @"csv" : @"text/csv",
                            @"pdf" : @"application/pdf",
                            @"vcard" : @"text/vcard",
                            @"json" : @"application/json",
                            @"zip" : @"application/zip",
                            @"text" : @"text/*",
                            @"mp3" : @"audio/mpeg",
                            @"wav" : @"audio/wav",
                            @"aiff" : @"audio/aiff",
                            @"flac" : @"audio/flac",
                            @"ogg" : @"audio/ogg",
                            @"xls" : @"application/vnd.ms-excel",
                            @"ics" : @"text/calendar",
                            @"xlsx" : @"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                        };
                        if([supportedMimeTypes objectForKey:attachmentType]) {
                            mimeType = [supportedMimeTypes objectForKey:attachmentType];
                        } else {
                            callback(@[[NSString stringWithFormat: @"Mime type '%@' for attachment is not handled", attachmentType]]);
                            return;
                        }
                    } else if (attachmentMimeType) {
                        mimeType = attachmentMimeType;
                    }

                    // Add attachment
                    [mail addAttachmentData:fileData mimeType:mimeType fileName:attachmentName];
                }
            }
        }

        UIViewController *root = [[[[UIApplication sharedApplication] delegate] window] rootViewController];

        while (root.presentedViewController) {
            root = root.presentedViewController;
        }
        [root presentViewController:mail animated:YES completion:nil];
    } else {
        callback(@[@"not_available"]);
    }
}

#pragma mark MFMailComposeViewControllerDelegate Methods

- (void)mailComposeController:(MFMailComposeViewController *)controller didFinishWithResult:(MFMailComposeResult)result error:(NSError *)error
{
    NSString *key = RCTKeyForInstance(controller);
    RCTResponseSenderBlock callback = _callbacks[key];
    if (callback) {
        switch (result) {
            case MFMailComposeResultSent:
                callback(@[[NSNull null] , @"sent"]);
                break;
            case MFMailComposeResultSaved:
                callback(@[[NSNull null] , @"saved"]);
                break;
            case MFMailComposeResultCancelled:
                callback(@[[NSNull null] , @"cancelled"]);
                break;
            case MFMailComposeResultFailed:
                callback(@[@"failed"]);
                break;
            default:
                callback(@[@"error"]);
                break;
        }
        [_callbacks removeObjectForKey:key];
    } else {
        RCTLogWarn(@"No callback registered for mail: %@", controller.title);
    }
    UIViewController *ctrl = [[[[UIApplication sharedApplication] delegate] window] rootViewController];
    while (ctrl.presentedViewController && ctrl != controller) {
        ctrl = ctrl.presentedViewController;
    }
    [ctrl dismissViewControllerAnimated:YES completion:nil];
}

#pragma mark Private

static NSString *RCTKeyForInstance(id instance)
{
    return [NSString stringWithFormat:@"%p", instance];
}

@end
