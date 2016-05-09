(ns syng-im.components.react
  (:require [reagent.core :as r]
            [syng-im.components.styles :as st]))

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
(def list-view (r/adapt-react-class (.-ListView js/React)))
(def text-input-class (r/adapt-react-class (.-TextInput js/React)))
(defn text-input [props text]
  [text-input-class (merge
                      {:underlineColorAndroid :transparent
                       :placeholderTextColor  st/text2-color}
                      props)
   text])


(defn icon [n style]
  [image {:source {:uri (keyword (str "icon_" (name n)))}
          :style  style}])

(def platform (.. js/React -Platform -OS))

(def android? (= platform "android"))

(defn list-item [component]
  (r/as-element component))

(def dismiss-keyboard! (js/require "dismissKeyboard"))

(comment
  (.-width (.get (.. js/React -Dimensions) "window"))
  )


;; (do
;;   (def activity-indicator-ios (r/adapt-react-class (.-ActivityIndicatorIOS js/React)))
;;   (def animated-image (r/adapt-react-class (.-Animated.Image js/React)))
;;   (def animated-text (r/adapt-react-class (.-Animated.Text js/React)))
;;   (def animated-view (r/adapt-react-class (.-Animated.View js/React)))
;;   (def date-picker-ios (r/adapt-react-class (.-DatePickerIOS js/React)))
;;   (def drawer-layout-android (r/adapt-react-class (.-DrawerLayoutAndroid js/React)))
;;   (def image (r/adapt-react-class (.-Image js/React)))
;;   (def list-view (r/adapt-react-class (.-ListView js/React)))
;;   (def map-view (r/adapt-react-class (.-MapView js/React)))
;;   (def modal (r/adapt-react-class (.-Modal js/React)))
;;   (def navigator (r/adapt-react-class (.-Navigator js/React)))
;;   (def navigator-ios (r/adapt-react-class (.-NavigatorIOS js/React)))
;;   (def picker-ios (r/adapt-react-class (.-PickerIOS js/React)))
;;   (def progress-bar-android (r/adapt-react-class (.-ProgressBarAndroid js/React)))
;;   (def progress-view-ios (r/adapt-react-class (.-ProgressViewIOS js/React)))
;;   (def pull-to-refresh-view-android (r/adapt-react-class (.-PullToRefreshViewAndroid js/React)))
;;   (def scroll-view (r/adapt-react-class (.-ScrollView js/React)))
;;   (def segmented-control-ios (r/adapt-react-class (.-SegmentedControlIOS js/React)))
;;   (def slider-ios (r/adapt-react-class (.-SliderIOS js/React)))
;;   (def switch (r/adapt-react-class (.-Switch js/React)))
;;   (def tab-bar-ios (r/adapt-react-class (.-TabBarIOS js/React)))
;;   (def tab-bar-ios-item (r/adapt-react-class (.-TabBarIOS.Item js/React)))
;;   (def text (r/adapt-react-class (.-Text js/React)))
;;   (def text-input (r/adapt-react-class (.-TextInput js/React)))
;;   (def toolbar-android (r/adapt-react-class (.-ToolbarAndroid js/React)))
;;   (def touchable-highlight (r/adapt-react-class (.-TouchableHighlight js/React)))
;;   (def touchable-native-feedback (r/adapt-react-class (.-TouchableNativeFeedback js/React)))
;;   (def touchable-opacity (r/adapt-react-class (.-TouchableOpacity js/React)))
;;   (def touchable-without-feedback (r/adapt-react-class (.-TouchableWithoutFeedback js/React)))
;;   (def view (r/adapt-react-class (.-View js/React)))
;;   (def view-pager-android (r/adapt-react-class (.-ViewPagerAndroid js/React)))
;;   (def web-view (r/adapt-react-class (.-WebView js/React))))
