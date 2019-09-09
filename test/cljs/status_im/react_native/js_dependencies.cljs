(ns status-im.react-native.js-dependencies)

(def action-button          #js {:default #js {:Item #js {}}})
(def config                 #js {:default #js {}})
(def camera                 #js {:default #js {:constants #js {}}})
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
       :Animated           #js {:View #js {}
                                :Text #js {}}
       :DeviceEventEmitter #js {:addListener (fn [])}
       :Dimensions         #js {:get  (fn [])}})

(def vector-icons           #js {:default #js {}})
(def webview-bridge         #js {:default #js {}})
(def webview                #js {:WebView #js {}})
(def status-keycard         #js {:default #js {}})

(defrecord Notification [])
(def react-native-firebase  #js {:default #js {:notifications #js {:Notification Notification}}})

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

(def keychain #js {:setGenericPassword (constantly (.resolve js/Promise true))})
(def react-navigation #js {:NavigationActions #js {}})
(def desktop-menu #js {})
(def desktop-config #js {})
(def react-native-mail #js {:mail #js {}})
(def react-native-navigation-twopane  #js {})
(def react-native-screens  #js {})
(def react-native-shake  #js {})
(def net-info  #js {})
(def touchid  #js {})
