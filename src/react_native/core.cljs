(ns react-native.core
  (:require ["react" :as react]
            ["react-native" :as react-native]
            [cljs-bean.core :as bean]
            [oops.core :as oops]
            [react-native.flat-list :as flat-list]
            [react-native.platform :as platform]
            [react-native.section-list :as section-list]
            [reagent.core :as reagent]))

(def app-state ^js (.-AppState ^js react-native))

;; Only use this component for exceptional cases, otherwise use Reanimated, e.g.
;; when using interpolated values exposed by RN Gesture Handler > Swipeable.
(def animated-view (reagent/adapt-react-class react-native/Animated.View))

(def view (reagent/adapt-react-class (.-View ^js react-native)))
(def scroll-view (reagent/adapt-react-class (.-ScrollView ^js react-native)))

(def ^:private image-native
  (reagent/adapt-react-class (.-Image ^js react-native)))

(defn image
  [{:keys [source] :as props}]
  [image-native
   (if (string? source)
     (assoc props :source {:uri source})
     props)])

(defn image-get-size [uri callback] (.getSize ^js (.-Image ^js react-native) uri callback))
(def text (reagent/adapt-react-class (.-Text ^js react-native)))
(def text-input (reagent/adapt-react-class (.-TextInput ^js react-native)))

(def pressable (reagent/adapt-react-class (.-Pressable ^js react-native)))
(def touchable-opacity (reagent/adapt-react-class (.-TouchableOpacity ^js react-native)))
(def touchable-highlight (reagent/adapt-react-class (.-TouchableHighlight ^js react-native)))
(def touchable-without-feedback
  (reagent/adapt-react-class (.-TouchableWithoutFeedback ^js react-native)))

(def flat-list flat-list/flat-list)

(def section-list section-list/section-list)

(def activity-indicator (reagent/adapt-react-class (.-ActivityIndicator ^js react-native)))

(def modal (reagent/adapt-react-class (.-Modal ^js react-native)))

(def keyboard ^js (.-Keyboard ^js react-native))

(def dismiss-keyboard! #(.dismiss keyboard))

(def device-event-emitter (.-DeviceEventEmitter ^js react-native))

(defn hide-splash-screen
  []
  (.hide ^js (-> react-native .-NativeModules .-SplashScreen)))

(defn alert
  [title message buttons options]
  (.alert (.-Alert ^js react-native) title message (clj->js buttons) (clj->js options)))

(def appearance ^js (.-Appearance ^js react-native))

(defn get-color-scheme
  []
  (.getColorScheme appearance))

(defn appearance-add-change-listener
  [handler]
  (.addChangeListener appearance handler))

(def get-window
  (memoize
   (fn []
     (js->clj (.get (.-Dimensions ^js react-native) "window") :keywordize-keys true))))

(def get-screen
  (memoize
   (fn []
     (js->clj (.get (.-Dimensions ^js react-native) "screen") :keywordize-keys true))))

(def small-screen?
  (let [height (:height (get-screen))]
    (< height 700)))

(defn hw-back-add-listener
  [callback]
  (.addEventListener (.-BackHandler ^js react-native) "hardwareBackPress" callback))

(defn hw-back-remove-listener
  [callback]
  (.removeEventListener (.-BackHandler ^js react-native) "hardwareBackPress" callback))

(def keyboard-avoiding-view-class (reagent/adapt-react-class (.-KeyboardAvoidingView react-native)))

(defn keyboard-avoiding-view
  [props & children]
  (into [keyboard-avoiding-view-class
         (merge (when platform/ios? {:behavior :padding})
                props)]
        children))

(def memo react/memo)

(def create-ref react/createRef)

(def use-ref react/useRef)

(defn current-ref
  [ref]
  (oops/oget ref "current"))

(def create-context react/createContext)

(def use-context react/useContext)

(defn use-effect
  ([effect-fn]
   (use-effect effect-fn []))
  ([effect-fn deps]
   (react/useEffect
    #(let [ret (effect-fn)]
       (if (fn? ret) ret js/undefined))
    (bean/->js deps))))

(def use-callback react/useCallback)

(defn use-effect-once
  [effect-fn]
  (use-effect effect-fn))

(defn use-unmount
  [f]
  (let [fn-ref (use-ref f)]
    (oops/oset! fn-ref "current" f)
    (use-effect-once (fn [] (fn [] (oops/ocall! fn-ref "current"))))))

(def layout-animation (.-LayoutAnimation ^js react-native))
(def configure-next (.-configureNext ^js layout-animation))

(def layout-animation-presets
  {:ease-in-ease-out (-> ^js layout-animation .-Presets .-easeInEaseOut)
   :linear           (-> ^js layout-animation .-Presets .-linear)
   :spring           (-> ^js layout-animation .-Presets .-spring)})

(def find-node-handle (.-findNodeHandle ^js react-native))

(defn selectable-text-input-manager
  []
  (when (exists? (.-NativeModules ^js react-native))
    (.-RNSelectableTextInputManager ^js (.-NativeModules ^js react-native))))

;; TODO: iOS native implementation https://github.com/status-im/status-mobile/issues/14137
(defonce selectable-text-input
  (if platform/android?
    (reagent/adapt-react-class
     (.requireNativeComponent ^js react-native "RNSelectableTextInput"))
    view))
