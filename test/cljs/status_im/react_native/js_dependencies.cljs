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
(def react-native-dark-mode #js {"eventEmitter" {} "initialMode" {}})

(def back-handler #js {:addEventListener identity
                       :removeEventListener identity})
(def react #js {:useCallback nil
                :useEffect nil
                :useRef nil
                :createRef nil
                :Fragment identity})
(def react-navigation-native #js {:NavigationContainer #js {}
                                  :useFocusEffect      identity
                                  :CommonActions       #js {}
                                  :StackActions        #js {}})
(def react-navigation-stack #js {:createStackNavigator identity
                                 :TransitionPresets    #js {:ModalPresentationIOS #js {}}})
(def react-navigation-bottom-tabs #js {:createBottomTabNavigator identity})

(def react-native-haptic-feedback #js {:default #js {:trigger nil}})

(def react-native-reanimated #js {:default      #js {:createAnimatedComponent identity
                                                     :eq                      nil
                                                     :greaterOrEq             nil
                                                     :add                     nil
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
                                                     :interpolate             nil
                                                     :call                    nil
                                                     :timing                  nil
                                                     :onChange                nil
                                                     :View                    #js {}
                                                     :ScrollView              #js {}
                                                     :Text                    #js {}
                                                     :extrapolate             #js {:CLAMP nil}
                                                     :Code                    #js {}}
                                  :Easing       #js {:bezier nil
                                                     :linear nil}
                                  :clockRunning nil})
(def react-native-gesture-handler #js {:default                  #js {}
                                       :State                    #js {:BEGAN        nil
                                                                      :ACTIVE       nil
                                                                      :CANCELLED    nil
                                                                      :END          nil
                                                                      :FAILED       nil
                                                                      :UNDETERMINED nil}
                                       :PureNativeButton         #js {}
                                       :TapGestureHandler        #js {}
                                       :PanGestureHandler        #js {}
                                       :LongPressGestureHandler  #js {}
                                       :TouchableWithoutFeedback #js {}
                                       :createNativeWrapper      identity})

(def react-native-redash #js {:clamp nil})
