(ns status-im.react-native.js-dependencies)

(def action-button          (fn [] #js {:default #js {:Item #js {}}}))
(def config                 (fn [] #js {:default #js {}}))
(def camera                 (fn [] #js {:default #js {:constants #js {}}}))
(def dialogs                (fn [] #js {}))
(def dismiss-keyboard       (fn [] #js {}))
(def emoji-picker           (fn [] #js {:default #js {}}))
(def fs                     (fn [] #js {}))
(def i18n                   #js {:locale "en"})
(def react-native-languages #js {:language "en", :addEventListener (fn []), :removeEventListener (fn [])})
(def image-crop-picker      (fn [] #js {}))
(def image-resizer          (fn [] #js {}))
(def qr-code                (fn [] #js {}))
(def react-native
  #js {:NativeModules      #js {}
       :Animated           #js {:View     #js {}
                                :FlatList #js {}
                                :Text     #js {}}
       :DeviceEventEmitter #js {:addListener (fn [])}
       :Dimensions         #js {:get  (fn [])}})
(def vector-icons           (fn [] #js {:default #js {}}))
(def webview                (fn [] #js {:WebView #js {}}))
(def touchid                (fn [] #js {}))
(def svg                    (fn [] #js {:default #js {}}))
(def status-keycard         (fn [] #js {:default #js {}}))

(def desktop-linking #js {:addEventListener (fn [])})
(def desktop-shortcuts #js {:addEventListener (fn [])})

(def snoopy                 (fn [] #js {:default #js {}}))
(def snoopy-filter          (fn [] #js {:default #js {}}))
(def snoopy-bars            (fn [] #js {:default #js {}}))
(def snoopy-buffer          (fn [] #js {:default #js {}}))

(def fetch (fn [_ _] #js {}))

(def background-timer (fn [] #js {:setTimeout    js/setTimeout
                                  :setInterval   js/setInterval
                                  :clearTimeout  js/clearTimeout
                                  :clearInterval js/clearInterval}))

(def keychain (fn [] #js {:setGenericPassword (constantly (.resolve js/Promise true))}))
(def react-navigation #js {:NavigationActions #js {}})
(def desktop-menu #js {})
(def desktop-config #js {})
(def react-native-mail (fn [] #js {:mail #js {}}))
(def react-native-navigation-twopane  #js {})
