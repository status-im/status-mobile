#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#define TF_DEPRECATED(x)  __attribute__ ((deprecated(x)))

@interface TestFairy: NSObject

/**
 * Initialize a TestFairy session.
 *
 * @param appToken Your key as given to you in your TestFairy account
 */
+ (void)begin:(NSString *)appToken;

/**
 * Initialize a TestFairy session with options.
 *
 * @param appToken Your key as given to you in your TestFairy account
 * @param options A dictionary of options controlling the current session
 */
+ (void)begin:(NSString *)appToken withOptions:(NSDictionary *)options;

/**
 * Change the server endpoint for use with on-premise hosting. Please
 * contact support or sales for more information. Must be called before begin
 *
 * @param serverOverride server address for use with TestFairy
 */
+ (void)setServerEndpoint:(NSString *)serverOverride;

/**
 * Returns SDK version (x.x.x) string
 *
 * @return version
 */
+ (NSString *)version;

/**
 * Hides a specific view from appearing in the video generated.
 *
 * @param view The specific view you wish to hide from screenshots
 *
 */
+ (void)hideView:(UIView *)view;

/**
 * Hides a specific html element from appearing in your UIWebView
 *
 * @param selector The specific selector you wish to hide from screenshots. Multiple selectors can be comma separated
 */
+ (void)hideWebViewElements:(NSString *)selector;

/**
 * Pushes the feedback view controller. Hook a button
 * to this method to allow users to provide feedback about the current
 * session. All feedback will appear in your build report page, and in
 * the recorded session page.
 *
 */
+ (void)pushFeedbackController;

/**
 * Send a feedback on behalf of the user. Call when using a in-house
 * feedback view controller with a custom design and feel. Feedback will
 * be associated with the current session.
 *
 * @param feedbackString Feedback text
 */
+ (void)sendUserFeedback:(NSString *)feedbackString;

/**
 * Proxy didUpdateLocation delegate values and these
 * locations will appear in the recorded sessions. Useful for debugging
 * actual long/lat values against what the user sees on screen.
 *
 * @param locations Array of CLLocation. The first object of the array will determine the user location
 */
+ (void)updateLocation:(NSArray *)locations;

/**
 * Marks a checkpoint in session. Use this text to tag a session
 * with a checkpoint name. Later you can filter sessions where your
 * user passed through this checkpoint, for bettering understanding
 * user experience and behavior.
 *
 * @param name The checkpoint name
 */
+ (void)checkpoint:(NSString *)name;

/**
 * Sets a correlation identifier for this session. This value can
 * be looked up via web dashboard. For example, setting correlation
 * to the value of the user-id after they logged in. Can be called
 * only once per session (subsequent calls will be ignored.)
 *
 * @param correlationId Id for the current session
 */
+ (void)setCorrelationId:(NSString *)correlationId TF_DEPRECATED("Please refer to setUser:");

/**
 * Sets a correlation identifier for this session. This value can
 * be looked up via web dashboard. For example, setting correlation
 * to the value of the user-id after they logged in. Can be called
 * only once per session (subsequent calls will be ignored.)
 *
 * @param correlationId Id for the current session
 */
+ (void)identify:(NSString *)correlationId TF_DEPRECATED("Please refer to setAttribute: and setUser:");

/**
 * Sets a correlation identifier for this session. This value can
 * be looked up via web dashboard. For example, setting correlation
 * to the value of the user-id after they logged in. Can be called
 * only once per session (subsequent calls will be ignored.)
 *
 * @param correlationId Id for the current session
 * @param traits Attributes and custom attributes to be associated with this session
 */
+ (void)identify:(NSString *)correlationId traits:(NSDictionary *)traits TF_DEPRECATED("Please refer to setAttribute:");

/**
 * Pauses the current session. This method stops recoding of
 * the current session until resume has been called.
 *
 * @see resume
 */
+ (void)pause;

/**
 * Resumes the recording of the current session. This method
 * resumes a session after it was paused.
 *
 * @see pause
 */
+ (void)resume;

/**
 * Returns the address of the recorded session on testfairy's
 * developer portal. Will return nil if recording not yet started.
 *
 * @return session URL
 */
+ (NSString *)sessionUrl;

/**
 * Takes a screenshot.
 *
 */
+ (void)takeScreenshot;

/**
 * Set the name of the current screen. Useful for single page
 * applications which use a single UIViewController.
 *
 * @param name logic name of current screen
 */
+ (void)setScreenName:(NSString *)name;

/**
 * Stops the current session recording. Unlike 'pause', when
 * calling 'resume', a new session will be created and will be
 * linked to the previous recording. Useful if you want short
 * session recordings of specific use-cases of the app. Hidden 
 * views and user identity will be applied to the new session 
 * as well, if started. 
 */
+ (void)stop;

/**
 * Records a session level attribute which can be looked up via web dashboard.
 *
 * @param name The name of the attribute. Cannot be nil.
 * @param value The value associated with the attribute. Cannot be nil.
 * @return YES if successfully set attribute value, NO if failed with error in log.
 *
 * @note The SDK limits you to storing 64 named attributes. Adding more than 64 will fail and return NO.
 */
+ (BOOL)setAttribute:(NSString *)key withValue:(NSString *)value;

/**
 * Records a user identified as an attribute. We recommend passing values such as
 * email, phone number, or user id that your app may use.
 *
 * @param userId The identifying user. Cannot be nil.
 *
 */
+ (void)setUserId:(NSString *)userId;

@end

#if __cplusplus
extern "C" {
#endif
	
/**
 * Remote logging, use TFLog as you would use printf. These logs will be sent to the server,
 * but will not appear in the console.
 *
 * @param format sprintf-like format for the arguments that follow
 */
void TFLog(NSString *format, ...) __attribute__((format(__NSString__, 1, 2)));

/**
 * Remote logging, use TFLogv as you would use printfv. These logs will be sent to the server,
 * but will not appear in the console.
 *
 * @param format sprintf-like format for the arguments that follow
 * @param arg_list list of arguments
 */
void TFLogv(NSString *format, va_list arg_list);
	
#if __cplusplus
}
#endif

extern NSString *const TFSDKIdentityTraitNameKey;
extern NSString *const TFSDKIdentityTraitEmailAddressKey;
extern NSString *const TFSDKIdentityTraitBirthdayKey;
extern NSString *const TFSDKIdentityTraitGenderKey;
extern NSString *const TFSDKIdentityTraitPhoneNumberKey;
extern NSString *const TFSDKIdentityTraitWebsiteAddressKey;
extern NSString *const TFSDKIdentityTraitAgeKey;
extern NSString *const TFSDKIdentityTraitSignupDateKey;
extern NSString *const TFSDKEnableCrashReporterKey;
extern NSString *const TestFairyDidShakeDevice;
extern NSString *const TestFairyWillProvideFeedback;
extern NSString *const TestFairyDidCancelFeedback;
extern NSString *const TestFairyDidSendFeedback;
