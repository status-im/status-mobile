(ns react-native.core
  (:require [reagent.core :as reagent]
            ["react-native" :as react-native]
            [react-native.flat-list :as flat-list]))

(def app-state ^js (.-AppState ^js react-native))

(def view (reagent/adapt-react-class (.-View ^js react-native)))
(def scroll-view (reagent/adapt-react-class (.-ScrollView ^js react-native)))
(def image (reagent/adapt-react-class (.-Image ^js react-native)))
(def text (reagent/adapt-react-class (.-Text ^js react-native)))
(def text-input (reagent/adapt-react-class  (.-TextInput ^js react-native)))

(def touchable-opacity (reagent/adapt-react-class (.-TouchableOpacity ^js react-native)))
(def touchable-highlight (reagent/adapt-react-class (.-TouchableHighlight ^js react-native)))
(def touchable-without-feedback (reagent/adapt-react-class (.-TouchableWithoutFeedback ^js react-native)))

(def flat-list flat-list/flat-list)

(def activity-indicator (reagent/adapt-react-class (.-ActivityIndicator ^js react-native)))

(def modal (reagent/adapt-react-class (.-Modal ^js react-native)))

(def keyboard ^js (.-Keyboard ^js react-native))

(def dismiss-keyboard! #(.dismiss keyboard))

(defn use-window-dimensions []
  (let [window ^js (react-native/useWindowDimensions)]
    {:font-scale (.-fontScale window)
     :height     (.-height window)
     :scale      (.-scale window)
     :width      (.-width window)}))

(defn hide-splash-screen []
  (.hide ^js (-> react-native .-NativeModules .-SplashScreen)))

(defn alert [title message buttons options]
  (.alert (.-Alert ^js react-native) title message (clj->js buttons) (clj->js options)))

(def appearance ^js (.-Appearance ^js react-native))

(defn get-color-scheme []
  (.getColorScheme appearance))

(defn appearance-add-change-listener [handler]
  (.addChangeListener appearance handler))

(defn get-window []
  (let [res (js->clj (.get (.-Dimensions ^js react-native) "window") :keywordize-keys true)]
    (println "WINDOW" (.-Dimensions ^js react-native) res)
    res))