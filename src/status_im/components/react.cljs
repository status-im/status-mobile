(ns status-im.components.react
  (:require [reagent.core :as r]
            [status-im.components.styles :as st]
            [status-im.utils.utils :as u]))

(def react-native (u/require "react-native"))
(def native-modules (.-NativeModules react-native))
(def geth (.-Geth native-modules))

(def linear-gradient-module (u/require "react-native-linear-gradient"))
(def dismiss-keyboard! (u/require "dismissKeyboard"))
(def orientation (u/require "react-native-orientation"))

;; Getters

(defn- get-react-property [name]
  (aget react-native name))

(defn- adapt-class [class]
  (when class
    (r/adapt-react-class class)))

(defn- get-class [name]
  (adapt-class (get-react-property name)))


;; React Components

(def app-registry (get-react-property "AppRegistry"))
(def navigator (get-class "Navigator"))
(def view (get-class "View"))
(def linear-gradient-class (adapt-class linear-gradient-module))

(def status-bar (get-class "StatusBar"))
(def drawer-layout-android (get-class "DrawerLayoutAndroid"))

(def list-view-class (get-class "ListView"))
(def scroll-view (get-class "ScrollView"))
(def web-view (get-class "WebView"))

(def text-class (get-class "Text"))
(def text-input-class (get-class "TextInput"))
(def image (get-class "Image"))

(def touchable-without-feedback (get-class "TouchableWithoutFeedback"))
(def touchable-highlight-class (get-class "TouchableHighlight"))
(def touchable-opacity (get-class "TouchableOpacity"))

(def modal (get-class "Modal"))
(def picker (get-class "Picker"))

(def pan-responder (.-PanResponder js/ReactNative))
(def animated (.-Animated js/ReactNative))
(def animated-view (r/adapt-react-class (.-View animated)))
(def animated-text (r/adapt-react-class (.-Text animated)))

(def dimensions (.-Dimensions js/ReactNative))
(def keyboard (.-Keyboard react-native))


;; Accessor methods for React Components

(defn text
  ([t]
   (r/as-element [text-class t]))
  ([{:keys [style platform-specific font] :as opts
     :or   {font :default}} t]
   (r/as-element
     [text-class
      (cond
        (and platform-specific font)
        (-> opts
            (dissoc :platform-specific :font)
            (assoc :style (st/with-font style platform-specific font)))
        style opts
        :else {:style opts})
      t])))

(defn text-input [props text]
  [text-input-class (merge
                      {:underlineColorAndroid :transparent
                       :placeholderTextColor  st/text2-color
                       :placeholder           "Type"}
                      props)
   text])

(defn icon
  ([n] (icon n {}))
  ([n style]
   [image {:source {:uri (keyword (str "icon_" (name n)))}
           :style  style}]))

(defn list-view [props]
  [list-view-class (merge {:enableEmptySections true} props)])

(defn touchable-highlight [props content]
  [touchable-highlight-class
   (merge {:underlay-color :transparent} props)
   content])

(def picker-item
  (when-let [picker (get-react-property "Picker")]
    (adapt-class (.-Item picker))))

(defn get-dimensions [name]
  (js->clj (.get dimensions name) :keywordize-keys true))

(defn linear-gradient
  [props & children]
  (vec (concat [linear-gradient-class (merge {:inverted true} props)] children)))


;; Platform

(def platform
  (when-let [pl (.-Platform react-native)] (.-OS pl)))

(def android? (= platform "android"))

(def ios? (= platform "ios"))

(defn list-item [component]
  (r/as-element component))

