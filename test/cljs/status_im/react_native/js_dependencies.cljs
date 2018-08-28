(ns status-im.react-native.js-dependencies)

(def action-button          #js {:default #js {:Item #js {}}})
(def config                 #js {:default #js {}})
(def camera                 #js {:constants #js {}})
(def dialogs                #js {})
(def dismiss-keyboard       #js {})
(def emoji-picker           #js {:default #js {}})
(def fs                     #js {})
(def http-bridge            #js {})
(def i18n                   #js {:locale "en"})
(def image-crop-picker      #js {})
(def image-resizer          #js {})
(def instabug               #js {})
(def linear-gradient        #js {})
(def nfc                    #js {})
(def orientation            #js {})
(def popup-menu             #js {})
(def qr-code                #js {})
(def react-native
  #js {:NativeModules      #js {}
       :Animated           #js {:View #js {}
                                :Text #js {}}
       :DeviceEventEmitter #js {:addListener (fn [])}
       :AsyncStorage       #js {:getItem (fn [a])
                                :setItem (fn [a b])}
       :Dimensions         #js {:get  (fn [])}})
(def async-storage         (.-AsyncStorage react-native))
(def realm                  #js {:schemaVersion (fn [])
                                 :defaultPath   "/tmp/realm"
                                 :close         (fn [])})
(def vector-icons           #js {:default #js {}})
(def webview-bridge         #js {:default #js {}})
(def svg                    #js {:default #js {}})

(defrecord Notification [])
(def react-native-firebase  #js {:default #js {:notifications #js {:Notification Notification}}})

(def snoopy                  #js {:default #js {}})
(def snoopy-filter           #js {:default #js {}})
(def snoopy-bars             #js {:default #js {}})
(def snoopy-buffer           #js {:default #js {}})
(def EventEmmiter            #js {})
(def fetch                   #js {})
(def testfairy               #js {})

(def background-timer       #js {:setTimeout js/setTimeout
                                 :setInterval js/setInterval
                                 :clearTimeout js/clearTimeout
                                 :clearInterval js/clearInterval})

(def keychain #js {:setGenericPassword (constantly (.resolve js/Promise true))})
(def secure-random #(.resolve js/Promise (clj->js (range 0 %))))
