(ns status-im.components.react
  (:require [reagent.core :as r]
            [status-im.components.styles :as st]))

(set! js/window.React (js/require "react-native"))

(def app-registry (.-AppRegistry js/React))
(def navigator (r/adapt-react-class (.-Navigator js/React)))
(def text (r/adapt-react-class (.-Text js/React)))
(def view (r/adapt-react-class (.-View js/React)))
(def image (r/adapt-react-class (.-Image js/React)))
(def touchable-highlight-class (r/adapt-react-class (.-TouchableHighlight js/React)))
(defn touchable-highlight [props content]
  [touchable-highlight-class
   (merge {:underlay-color :transparent} props)
   content])
(def toolbar-android (r/adapt-react-class (.-ToolbarAndroid js/React)))
(def list-view-class (r/adapt-react-class (.-ListView js/React)))
(defn list-view [props]
  [list-view-class (merge {:enableEmptySections true} props)])
(def scroll-view (r/adapt-react-class (.-ScrollView js/React)))
(def touchable-without-feedback (r/adapt-react-class (.-TouchableWithoutFeedback js/React)))
(def text-input-class (r/adapt-react-class (.-TextInput js/React)))
(defn text-input [props text]
  [text-input-class (merge
                      {:underlineColorAndroid :transparent
                       :placeholderTextColor  st/text2-color
                       :placeholder           "Type"}
                      props)
   text])
(def drawer-layout-android (r/adapt-react-class (.-DrawerLayoutAndroid js/React)))
(def touchable-opacity (r/adapt-react-class (.-TouchableOpacity js/React)))
(def modal (r/adapt-react-class (.-Modal js/React)))
(def picker (r/adapt-react-class (.-Picker js/React)))
(def picker-item (r/adapt-react-class (.-Item (.-Picker js/React))))

(def animated (.-Animated js/React))
(def animated-view (r/adapt-react-class (.-View animated)))
(def animated-text (r/adapt-react-class (.-Text animated)))


(defn icon
  ([n] (icon n {}))
  ([n style]
   [image {:source {:uri (keyword (str "icon_" (name n)))}
           :style  style}]))

;(def react-linear-gradient (.-default (js/require "react-native-linear-gradient")))
;(def linear-gradient (r/adapt-react-class react-linear-gradient))

(set! js/window.LinearGradient (js/require "react-native-linear-gradient"))
(defn linear-gradient [props]
  (js/React.createElement js/LinearGradient
                          (clj->js (merge {:inverted true} props))))


(def platform (.. js/React -Platform -OS))

(def android? (= platform "android"))

(defn list-item [component]
  (r/as-element component))

(def dismiss-keyboard! (js/require "dismissKeyboard"))
