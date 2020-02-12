(ns status-im.react-native.js-dependencies)

(def action-button          #js {:default #js {:Item #js {}}})
(def config                 #js {:default #js {}})
(def camera                 #js {:RNCamera #js {:Constants #js {}}})
(def dialogs                #js {})
(def dismiss-keyboard       #js {})
(def emoji-picker           #js {:default #js {}})
(def fs                     #js {})
(def i18n                   #js {:locale "en"})
(def react-native-languages #js {:language "en", :addEventListener (fn []), :removeEventListener (fn [])})
(def image-crop-picker      #js {})
(def image-resizer          #js {})
(def qr-code                #js {})
(def svg                    #js {})

(def react-native
  #js {:NativeModules      #js {}
       :Animated           #js {:View     #js {}
                                :FlatList #js {}
                                :Text     #js {}}
       :Easing             #js {:bezier (fn [])}
       :DeviceEventEmitter #js {:addListener (fn [])}
       :Dimensions         #js {:get (fn [])}})

(set! js/ReactNative react-native)

(def vector-icons           #js {:default #js {}})
(def webview                #js {:WebView #js {}})
(def status-keycard         #js {:default #js {}})

(def desktop-linking #js {:addEventListener (fn [])})
(def desktop-shortcuts #js {:addEventListener (fn [])})

(def snoopy                 #js {:default #js {}})
(def snoopy-filter          #js {:default #js {}})
(def snoopy-bars            #js {:default #js {}})
(def snoopy-buffer          #js {:default #js {}})
(def fetch                  #js {})

(def background-timer       #js {:setTimeout js/setTimeout
                                 :setInterval js/setInterval
                                 :clearTimeout js/clearTimeout
                                 :clearInterval js/clearInterval})

(def keychain #js {:setGenericPassword (constantly (.resolve js/Promise true))
                   "ACCESSIBLE" {}
                   "ACCESS_CONTROL" {}})
(def desktop-menu #js {})
(def desktop-config #js {})
(def react-native-mail #js {:mail #js {}})
(def react-native-navigation-twopane  #js {})
(def react-native-screens  #js {})
(def react-native-shake  #js {})
(def net-info  #js {})
(def touchid  #js {})
(def safe-area-context #js {})

(def back-handler #js {:addEventListener identity
                       :removeEventListener identity})
(def react #js {:useCallback nil})
(def react-navigation-native #js {:NavigationContainer #js {}
                                  :useFocusEffect      identity
                                  :CommonActions       #js {}
                                  :StackActions        #js {}})
(def react-navigation-stack #js {:createStackNavigator identity
                                 :TransitionPresets    #js {:ModalPresentationIOS #js {}}})
(def react-navigation-bottom-tabs #js {:createBottomTabNavigator identity})
