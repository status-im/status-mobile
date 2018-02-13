(ns status-im.react-native.js-dependencies)

(def action-button          #js {:default #js {:Item #js {}}})
(def autolink               #js {:default #js {}})
(def config                 #js {:default #js {}})
(def camera                 #js {:default #js {:constants #js {}}})
(def dialogs                #js {})
(def dismiss-keyboard       #js {})
(def emoji-picker           #js {:default #js {}})
(def fs                     #js {})
(def http-bridge            #js {})
(def i18n                   #js {})
(def image-crop-picker      #js {})
(def image-resizer          #js {})
(def instabug               #js {})
(def linear-gradient        #js {})
(def nfc                    #js {})
(def orientation            #js {})
(def popup-menu             #js {})
(def qr-code                #js {})
(def random-bytes           #js {})
(def react-native
  #js {:NativeModules      #js {}
       :Animated           #js {:View #js {}
                                :Text #js {}}
       :DeviceEventEmitter #js {:addListener (fn [])}
       :Dimensions         #js {:get  (fn [])}})
(def realm                  #js {:schemaVersion (fn [])
                                 :close         (fn [])})
(def sortable-listview      #js {})
(def vector-icons           #js {:default #js {}})
(def webview-bridge         #js {:default #js {}})
(def svg                    #js {:default #js {}})
(def react-native-fcm       #js {:default #js {}})

(def snoopy                  #js {:default #js {}})
(def snoopy-filter           #js {:default #js {}})
(def snoopy-bars             #js {:default #js {}})
(def snoopy-buffer           #js {:default #js {}})
(def EventEmmiter            #js {})

(def background-timer       #js {:setTimeout js/setTimeout
                                 :setInterval js/setInterval
                                 :clearTimeout js/clearTimeout
                                 :clearInterval js/clearInterval})
