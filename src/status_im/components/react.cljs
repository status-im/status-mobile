(ns status-im.components.react
  (:require [reagent.core :as r]
            [status-im.components.styles :as st]))

(when (exists? js/window)
  (set! js/window.React (js/require "react-native")))

(def react (js/require "react-native"))

(def app-registry (.-AppRegistry react))
(def navigator (r/adapt-react-class (.-Navigator react)))
(def text (r/adapt-react-class (.-Text react)))
(def view (r/adapt-react-class (.-View react)))
(def image (r/adapt-react-class (.-Image react)))
(def touchable-highlight-class (r/adapt-react-class (.-TouchableHighlight react)))
(defn touchable-highlight [props content]
  [touchable-highlight-class
   (merge {:underlay-color :transparent} props)
   content])
(def toolbar-android (r/adapt-react-class (.-ToolbarAndroid react)))
(def list-view-class (r/adapt-react-class (.-ListView react)))
(defn list-view [props]
  [list-view-class (merge {:enableEmptySections true} props)])
(def scroll-view (r/adapt-react-class (.-ScrollView react)))
(def touchable-without-feedback (r/adapt-react-class (.-TouchableWithoutFeedback react)))
(def text-input-class (r/adapt-react-class (.-TextInput react)))
(defn text-input [props text]
  [text-input-class (merge
                      {:underlineColorAndroid :transparent
                       :placeholderTextColor  st/text2-color
                       :placeholder           "Type"}
                      props)
   text])
(def drawer-layout-android (r/adapt-react-class (.-DrawerLayoutAndroid react)))
(def touchable-opacity (r/adapt-react-class (.-TouchableOpacity react)))
(def modal (r/adapt-react-class (.-Modal react)))
(def picker (r/adapt-react-class (.-Picker react)))
(def picker-item (r/adapt-react-class (.-Item (.-Picker react))))


(defn icon
  ([n] (icon n {}))
  ([n style]
   [image {:source {:uri (keyword (str "icon_" (name n)))}
           :style  style}]))

;(def react-linear-gradient (.-default (js/require "react-native-linear-gradient")))
;(def linear-gradient (r/adapt-react-class react-linear-gradient))

(def linear-gradient-class (js/require "react-native-linear-gradient"))
(defn linear-gradient [props]
  (r/creacteElement linear-gradient-class
                    (clj->js (merge {:inverted true} props))))


(def platform (.. react -Platform -OS))

(def android? (= platform "android"))

(defn list-item [component]
  (r/as-element component))

(def dismiss-keyboard! (js/require "dismissKeyboard"))
