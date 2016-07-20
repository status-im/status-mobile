(ns status-im.components.react
  (:require [reagent.core :as r]
            [status-im.components.styles :as st]
            [status-im.utils.utils :as u]))

(def react-native (u/require "react-native"))
(def native-modules (.-NativeModules react-native))

(defn get-react-property [name]
  (aget react-native name))

(defn adapt-class [class]
  (when class (r/adapt-react-class class)))

(defn get-class [name]
  (adapt-class (get-react-property name)))

(def app-registry (get-react-property "AppRegistry"))
(def navigator (get-class "Navigator"))
(def text-class (get-class "Text"))
(defn text
  ([s] [text-class s])
  ([{:keys [style] :as opts} s]
   [text-class (if style opts {:style opts}) s]))
(def view (get-class "View"))
(def image (get-class "Image"))
(def touchable-highlight-class (get-class "TouchableHighlight"))
(defn touchable-highlight [props content]
  [touchable-highlight-class
   (merge {:underlay-color :transparent} props)
   content])
(def toolbar-android (get-class "ToolbarAndroid"))
(def list-view-class (get-class "ListView"))
(defn list-view [props]
  [list-view-class (merge {:enableEmptySections true} props)])
(def scroll-view (get-class "ScrollView"))
(def web-view (get-class "WebView"))
(def touchable-without-feedback (get-class "TouchableWithoutFeedback"))
(def text-input-class (get-class "TextInput"))
(defn text-input [props text]
  [text-input-class (merge
                      {:underlineColorAndroid :transparent
                       :placeholderTextColor  st/text2-color
                       :placeholder           "Type"}
                      props)
   text])
(def drawer-layout-android (get-class "DrawerLayoutAndroid"))
(def touchable-opacity (get-class "TouchableOpacity"))
(def modal (get-class "Modal"))
(def picker (get-class "Picker"))
(def picker-item
  (when-let [picker (get-react-property "Picker")]
    (adapt-class (.-Item picker))))

(def pan-responder (.-PanResponder js/ReactNative))
(def animated (.-Animated js/ReactNative))
(def animated-view (r/adapt-react-class (.-View animated)))
(def animated-text (r/adapt-react-class (.-Text animated)))

(def dimensions (.-Dimensions js/ReactNative))
(defn get-dimensions [name]
  (js->clj (.get dimensions name) :keywordize-keys true))

(defn icon
  ([n] (icon n {}))
  ([n style]
   [image {:source {:uri (keyword (str "icon_" (name n)))}
           :style  style}]))

(def linear-gradient-class
  (r/adapt-react-class (u/require "react-native-linear-gradient")))
(defn linear-gradient
  [props & children]
  (vec (concat [linear-gradient-class (merge {:inverted true} props)] children)))


(def platform
  (when-let [pl (.-Platform react-native)] (.-OS pl)))

(def android? (= platform "android"))

(defn list-item [component]
  (r/as-element component))

(def orientation (u/require "react-native-orientation"))
(def keyboard (.-Keyboard react-native))

(def geth (.-Geth native-modules))
