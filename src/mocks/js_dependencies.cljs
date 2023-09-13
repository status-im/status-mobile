(ns mocks.js-dependencies
  (:require-macros [status-im.utils.slurp :refer [slurp]])
  (:require [status-im.fleet.default-fleet :refer (default-fleets)])
  (:require [status-im.utils.test :as utils.test]))

;; to generate a js Proxy at js/__STATUS_MOBILE_JS_IDENTITY_PROXY__ that accept any (.xxx) call and
;; return itself
;; For the convenience to mock eg.
;; (-> reanimated/slide-out-up-animation .springify (.damping 20) (.stiffness 300))
;; (-> reanimated/slide-out-up-animation (.damping 20) .springify (.stiffness 300))
(js/eval
 "
 var globalThis
 if (typeof window === \"undefined\") {
   globalThis = global
 } else {
   globalThis = window
 }
 globalThis.__STATUS_MOBILE_JS_IDENTITY_PROXY__ = new Proxy({}, {get() { return () => globalThis.__STATUS_MOBILE_JS_IDENTITY_PROXY__}})
 ")

(def action-button #js {:default #js {:Item #js {}}})
(def config #js {:default #js {}})
(def camera #js {:RNCamera #js {:Constants #js {}}})
(def dialogs #js {})
(def dismiss-keyboard #js {})
(def emoji-picker #js {:default #js {}})
(def fs #js {})
(def i18n #js {:locale "en"})
(def image-crop-picker #js {})
(def image-resizer #js {})
(def qr-code #js {})
(def svg #js {})

(def react-native
  (clj->js
   {:NativeModules            {:RNGestureHandlerModule {:Direction (fn [])}
                               :PushNotifications      {}
                               :Status                 utils.test/status
                               :ReanimatedModule       {:configureProps (fn [])}}

    :View                     {}
    :RefreshControl           {}
    :AppState                 {}
    :Alert                    {:alert (fn [])}
    :FlatList                 {}
    :SectionList              {}
    :Text                     {}
    :StatusBar                {}
    :ScrollView               {}
    :KeyboardAvoidingView     {}
    :TextInput                {}
    :Image                    {}
    :Picker                   {:Item {}}
    :Switch                   {}
    :Modal                    {}
    :Keyboard                 {:dismiss (fn [])}
    :Linking                  {}
    :TouchableWithoutFeedback {}
    :TouchableHighlight       {}
    :Pressable                {}
    :TouchableOpacity         {}
    :ActivityIndicator        {}
    :StyleSheet               {:create (fn [])}
    :Animated                 {:createAnimatedComponent identity
                               :Value                   (fn [])
                               :ValueXY                 (fn [])
                               :View                    {}
                               :FlatList                {}
                               :ScrollView              {}
                               :Text                    {}}
    :Easing                   {:bezier (fn [])
                               :poly   (fn [])
                               :out    (fn [])
                               :in     (fn [])
                               :inOut  (fn [])}
    :DeviceEventEmitter       {:addListener (fn [])}
    :Dimensions               {:get (fn []) :addEventListener identity}
    :useWindowDimensions      {}
    :Platform                 {:select (fn [])}
    :I18nManager              {:isRTL ""}
    :NativeEventEmitter       (fn [])
    :LayoutAnimation          {:Presets       #js
                                               {:easeInEaseOut nil
                                                :linear        nil
                                                :spring        nil}
                               :Types         #js {}
                               :Properties    #{}
                               :create        (fn [])
                               :configureNext (fn [])}
    :requireNativeComponent   (fn [] {:propTypes ""})
    :Appearance               {:getColorScheme    (fn [])
                               :addChangeListener (fn [])}
    :PixelRatio               {:get (fn [])}}))

(set! js/ReactNative react-native)

(def reanimated-bottom-sheet #js {:default #js {}})

(def icons #js {:default #js {}})
(def webview #js {:WebView #js {}})
(def status-keycard
  #js
   {:default #js
              {:nfcIsSupported (fn [] #js {:then identity})
               :nfcIsEnabled   (fn [] #js {:then identity})}})

(def snoopy #js {:default #js {}})
(def snoopy-filter #js {:default #js {}})
(def snoopy-bars #js {:default #js {}})
(def snoopy-buffer #js {:default #js {}})
(def fetch #js {})

(def async-storage-atom (atom {}))
(def async-storage
  #js
   {:default #js
              {:setItem  #(js/Promise.resolve)
               :multiGet #(js/Promise.resolve)
               :getItem  #(js/Promise.resolve)}})

(def background-timer
  (clj->js {:default {:setTimeout    js/setTimeout
                      :setInterval   js/setInterval
                      :clearTimeout  js/clearTimeout
                      :clearInterval js/clearInterval}}))

(def keychain
  #js
   {:setGenericPassword       (constantly (.resolve js/Promise true))
    :setInternetCredentials   #(js/Promise.resolve)
    :resetInternetCredentials #(js/Promise.resolve)
    "ACCESSIBLE"              {}
    "ACCESS_CONTROL"          {}})

(def react-native-mail #js {:mail #js {}})
(def react-native-screens #js {})
(def react-native-shake #js {})
(def react-native-share #js {:default {}})
(def react-native-svg
  #js
   {:ClipPath       #js {:render identity}
    :Circle         #js {:render identity}
    :Defs           #js {:render identity}
    :G              #js {:render identity}
    :Path           #js {:render identity}
    :Rect           #js {:render identity}
    :SvgUri         #js {:render identity}
    :SvgXml         #js {:render identity}
    :LinearGradient #js {:render identity}
    :Stop           #js {:render identity}
    :default        #js {:render identity}})
(def react-native-webview #js {:default {}})
(def react-native-audio-toolkit
  #js
   {:MediaStates        {}
    :PlaybackCategories {}})
(def net-info #js {})
(def touchid #js {})
(def react-native-image-viewing #js {:default {}})
(def react-native-static-safe-area-insets #js {:default {}})

(def back-handler
  #js
   {:addEventListener    identity
    :removeEventListener identity})
(def react
  #js
   {:useCallback nil
    :useEffect   nil
    :useRef      nil
    :createRef   nil
    :Fragment    identity})
(def react-navigation-native
  #js
   {:NavigationContainer #js {}
    :useFocusEffect      identity
    :CommonActions       #js {}
    :StackActions        #js {}})

(def react-native-navigation
  #js
   {:Navigation #js
                 {:constants (fn [] #js {:then identity})
                  :setDefaultOptions identity
                  :setRoot identity
                  :dismissOverlay #(js/Promise.resolve)
                  :showOverlay identity
                  :setLazyComponentRegistrator identity
                  :pop identity
                  :push identity
                  :mergeOptions identity
                  :popToRoot identity
                  :showModal identity
                  :dismissModal identity
                  :registerComponent identity
                  :events
                  (fn []
                    #js
                     {:registerModalDismissedListener          identity
                      :registerAppLaunchedListener             identity
                      :registerBottomTabSelectedListener       identity
                      :registerComponentDidDisappearListener   identity
                      :registerComponentDidAppearListener      identity
                      :registerNavigationButtonPressedListener identity})}})

(def react-navigation-stack
  #js
   {:createStackNavigator identity
    :TransitionPresets    #js {:ModalPresentationIOS #js {}}})
(def react-navigation-bottom-tabs #js {:createBottomTabNavigator identity})

(def react-native-haptic-feedback #js {:default #js {:trigger nil}})

(def react-native-reanimated
  #js
   {:default                #js
                             {:createAnimatedComponent identity
                              :eq                      nil
                              :greaterOrEq             nil
                              :greaterThan             nil
                              :lessThan                nil
                              :lessOrEq                nil
                              :add                     nil
                              :diff                    nil
                              :divide                  nil
                              :sub                     nil
                              :multiply                nil
                              :abs                     nil
                              :min                     nil
                              :max                     nil
                              :neq                     nil
                              :and                     nil
                              :or                      nil
                              :not                     nil
                              :set                     nil
                              :startClock              nil
                              :stopClock               nil
                              :Value                   nil
                              :Clock                   nil
                              :debug                   nil
                              :log                     nil
                              :event                   nil
                              :cond                    nil
                              :block                   nil
                              :interpolateNode         nil
                              :call                    nil
                              :timing                  nil
                              :onChange                nil
                              :View                    #js {}
                              :Image                   #js {}
                              :ScrollView              #js {}
                              :Text                    #js {}
                              :Extrapolate             #js {:CLAMP nil}
                              :Code                    #js {}}
    :EasingNode             #js
                             {:bezier identity
                              :linear identity}
    :clockRunning           nil
    :useSharedValue         (fn [])
    :useAnimatedStyle       (fn [])
    :withTiming             (fn [])
    :withDelay              (fn [])
    :Easing                 #js {:bezier identity}
    :Keyframe               (fn [])
    :enableLayoutAnimations (fn [])
    :SlideOutUp             js/__STATUS_MOBILE_JS_IDENTITY_PROXY__
    :SlideInUp              js/__STATUS_MOBILE_JS_IDENTITY_PROXY__
    :LinearTransition       js/__STATUS_MOBILE_JS_IDENTITY_PROXY__})

(def react-native-gesture-handler
  #js
   {:FlatList                 #js {}
    :Gesture                  #js {:Pan nil}
    :GestureDetector          #js {}
    :LongPressGestureHandler  #js {}
    :NativeViewGestureHandler #js {}
    :PanGestureHandler        #js {}
    :PureNativeButton         #js {}
    :RectButton               #js {}
    :ScrollView               #js {}
    :State                    #js
                               {:BEGAN        nil
                                :ACTIVE       nil
                                :CANCELLED    nil
                                :END          nil
                                :FAILED       nil
                                :UNDETERMINED nil}
    :Swipeable                #js {}
    :TapGestureHandler        #js {}
    :TouchableHighlight       #js {}
    :TouchableOpacity         #js {}
    :TouchableWithoutFeedback #js {}
    :createNativeWrapper      identity
    :default                  #js {}})

(def react-native-redash
  #js
   {:clamp     nil
    :withPause (fn [])})

(def react-native-languages
  (clj->js {:default {:language            "en"
                      :addEventListener    (fn [])
                      :removeEventListener (fn [])}}))

(def react-native-device-info
  #js {:getInstallReferrer identity})

(def react-native-camera-kit
  #js
   {:Camera     #js {}
    :CameraType #js {}})

(def react-native-push-notification
  #js
   {:localNotification identity
    :requestPermission identity})

(def react-native-gradien #js {:default #js {}})

(def masked-view #js {:default #js {}})

(def react-native-permissions #js {:default #js {}})

(def push-notification-ios #js {:default #js {:abandonPermissions identity}})

(def rn-emoji-keyboard
  #js {:EmojiKeyboard #js {}})

(def react-native-hole-view
  #js {:RNHoleView #js {}})

(def react-native-draggable-flatlist
  #js {:default #js {}})

(def react-native-blob-util
  #js {:default #js {}})

(def react-native-blur
  (clj->js {:BlurView {}}))

(def react-native-camera-roll
  (clj->js {:default #js {}}))

(def react-native-orientation-locker
  (clj->js {:default                    #js {}
            :useDeviceOrientationChange #js {}}))

(def react-native-transparent-video #js {:default #js {}})

(def react-native-gifted-charts
  #js
   {:BarChart         #js {}
    :PieChart         #js {}
    :LineChart        #js {}
    :LineChartBicolor #js {}})

(def wallet-connect-client
  #js
   {:default       #js {}
    :CLIENT_EVENTS #js
                    {:session #js
                               {:request  nil
                                :created  nil
                                :deleted  nil
                                :proposal nil
                                :updated  nil}}})

(def worklet-factory
  #js {:applyAnimationsToStyle (fn [])})

;; Update i18n_resources.cljs
(defn mock
  [module]
  (case module
    "react-native-languages"                         react-native-languages
    "react-native-background-timer"                  background-timer
    "react-native-image-crop-picker"                 image-crop-picker
    "react-native-gesture-handler"                   react-native-gesture-handler
    "react-native-static-safe-area-insets"           react-native-static-safe-area-insets
    "react-native-config"                            config
    "react-native-iphone-x-helper"                   (clj->js {:getStatusBarHeight (fn [])
                                                               :getBottomSpace     (fn [])})
    "react-native-screens"                           (clj->js {})
    "react-native-reanimated"                        react-native-reanimated
    "react-native-redash/lib/module/v1"              react-native-redash
    "react-native-redash"                            react-native-redash
    "react-native-fetch-polyfill"                    fetch
    "react-native-status-keycard"                    status-keycard
    "react-native-keychain"                          keychain
    "react-native-touch-id"                          touchid
    "@react-native-community/netinfo"                net-info
    "react-native-dialogs"                           dialogs
    "react-native"                                   react-native
    "react-native-fs"                                fs
    "react-native-mail"                              react-native-mail
    "react-native-image-resizer"                     image-resizer
    "react-native-haptic-feedback"                   react-native-haptic-feedback
    "react-native-device-info"                       react-native-device-info
    "react-native-push-notification"                 react-native-push-notification
    "react-native-linear-gradient"                   react-native-gradien
    "@react-native-community/masked-view"            masked-view
    "react-native-blob-util"                         react-native-blob-util
    "react-native-navigation"                        react-native-navigation
    "@react-native-community/push-notification-ios"  push-notification-ios
    "@react-native-community/blur"                   react-native-blur
    "@react-native-community/cameraroll"             react-native-camera-roll
    "react-native-camera-kit"                        react-native-camera-kit
    "react-native-permissions"                       react-native-permissions
    "rn-emoji-keyboard"                              rn-emoji-keyboard
    "react-native-hole-view"                         react-native-hole-view
    "react-native-draggable-flatlist"                react-native-draggable-flatlist
    "react-native-webview"                           react-native-webview
    "@react-native-community/audio-toolkit"          react-native-audio-toolkit
    "react-native-image-viewing"                     react-native-image-viewing
    "react-native-share"                             react-native-share
    "@react-native-async-storage/async-storage"      async-storage
    "react-native-svg"                               react-native-svg
    "react-native-transparent-video"                 react-native-transparent-video
    "react-native-orientation-locker"                react-native-orientation-locker
    "react-native-gifted-charts"                     react-native-gifted-charts
    "../resources/data/emojis/en.json"               (js/JSON.parse (slurp
                                                                     "./resources/data/emojis/en.json"))
    "../src/js/worklets/core.js"                     worklet-factory
    "../src/js/worklets/shell/bottom_tabs.js"        #js {}
    "../src/js/worklets/shell/home_stack.js"         #js {}
    "../src/js/worklets/shell/floating_screen.js"    #js {}
    "../src/js/worklets/bottom_sheet.js"             #js {}
    "../src/js/worklets/record_audio.js"             #js {}
    "../src/js/worklets/scroll_view.js"              #js {}
    "../src/js/worklets/onboarding_carousel.js"      #js {}
    "../src/js/worklets/lightbox.js"                 #js {}
    "../src/js/worklets/parallax.js"                 #js {}
    "../src/js/worklets/identifiers_highlighting.js" #js {}
    "./fleets.js"                                    default-fleets
    "@walletconnect/client"                          wallet-connect-client
    "../translations/ar.json"                        (js/JSON.parse (slurp "./translations/ar.json"))
    "../translations/de.json"                        (js/JSON.parse (slurp "./translations/de.json"))
    "../translations/en.json"                        (js/JSON.parse (slurp "./translations/en.json"))
    "../translations/es.json"                        (js/JSON.parse (slurp "./translations/es.json"))
    "../translations/es_419.json"                    (js/JSON.parse (slurp "./translations/es_419.json"))
    "../translations/fil.json"                       (js/JSON.parse (slurp "./translations/fil.json"))
    "../translations/fr.json"                        (js/JSON.parse (slurp "./translations/fr.json"))
    "../translations/id.json"                        (js/JSON.parse (slurp "./translations/id.json"))
    "../translations/it.json"                        (js/JSON.parse (slurp "./translations/it.json"))
    "../translations/ko.json"                        (js/JSON.parse (slurp "./translations/ko.json"))
    "../translations/pt_BR.json"                     (js/JSON.parse (slurp "./translations/pt_BR.json"))
    "../translations/ru.json"                        (js/JSON.parse (slurp "./translations/ru.json"))
    "../translations/tr.json"                        (js/JSON.parse (slurp "./translations/tr.json"))
    "../translations/zh.json"                        (js/JSON.parse (slurp "./translations/zh.json"))
    "../translations/zh_hant.json"                   (js/JSON.parse (slurp
                                                                     "./translations/zh_hant.json"))
    "../translations/zh_TW.json"                     (js/JSON.parse (slurp "./translations/zh_TW.json"))
    nil))
