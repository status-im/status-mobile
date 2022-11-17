(ns react-native.core
  (:require [reagent.core :as reagent]
            ["react-native" :as react-native]
            ["@react-native-community/blur" :as blur]
            [react-native.flat-list :as flat-list]
<<<<<<< HEAD
<<<<<<< HEAD
            [react-native.section-list :as section-list]))
=======
            ["react-native-navigation" :refer (Navigation)]))
>>>>>>> 3c47804f9... refactor
=======
            [react-native.section-list :as section-list]
            ["react-native-navigation" :refer (Navigation)]
            [react-native.platform :as platform]))
>>>>>>> ca683e4a1... add SectionList to RN

(def app-state ^js (.-AppState ^js react-native))
(def blur-view (reagent/adapt-react-class (.-BlurView blur)))

(def view (reagent/adapt-react-class (.-View ^js react-native)))
(def scroll-view (reagent/adapt-react-class (.-ScrollView ^js react-native)))
(def image (reagent/adapt-react-class (.-Image ^js react-native)))
(def text (reagent/adapt-react-class (.-Text ^js react-native)))
(def text-input (reagent/adapt-react-class (.-TextInput ^js react-native)))

(def touchable-opacity (reagent/adapt-react-class (.-TouchableOpacity ^js react-native)))
(def touchable-highlight (reagent/adapt-react-class (.-TouchableHighlight ^js react-native)))
(def touchable-without-feedback (reagent/adapt-react-class (.-TouchableWithoutFeedback ^js react-native)))

(def flat-list flat-list/flat-list)

(def section-list section-list/section-list)

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
  (js->clj (.get (.-Dimensions ^js react-native) "window") :keywordize-keys true))

(def status-bar (.-StatusBar ^js react-native))

(def navigation-const (atom nil))

(.then (.constants Navigation)
       (fn [^js consts]
         (reset! navigation-const {:top-bar-height     (.-topBarHeight consts)
                                   :bottom-tabs-height (.-bottomTabsHeight consts)
                                   :status-bar-height  (.-statusBarHeight consts)})))

(def keyboard-avoiding-view-class (reagent/adapt-react-class (.-KeyboardAvoidingView ^js react-native)))

(defn keyboard-avoiding-view []
  (let [this  (reagent/current-component)
        props (reagent/props this)]
    (into [keyboard-avoiding-view-class
           (merge (when platform/ios?
                    {:behavior :padding})
                  props
                  {:keyboardVerticalOffset (+ 44 (:status-bar-height @navigation-const))})]
          (reagent/children this))))
