(ns react-native.core
  (:require
    ["react" :as react]
    ["react-native" :as react-native]
    [oops.core :as oops]
    [react-native.flat-list :as flat-list]
    [react-native.platform :as platform]
    [react-native.section-list :as section-list]
    [react-native.utils :as utils]
    [reagent.core :as reagent]))

(def app-state ^js (.-AppState ^js react-native))

;; Only use this component for exceptional cases, otherwise use Reanimated, e.g.
;; when using interpolated values exposed by RN Gesture Handler > Swipeable.
(def animated-view (reagent/adapt-react-class react-native/Animated.View))

(def view (reagent/adapt-react-class (.-View ^js react-native)))
(def scroll-view (reagent/adapt-react-class (.-ScrollView ^js react-native)))
(def safe-area-view (reagent/adapt-react-class (.-SafeAreaView ^js react-native)))

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

(def pressable-class (reagent/adapt-react-class (.-Pressable ^js react-native)))
(def touchable-opacity-class (reagent/adapt-react-class (.-TouchableOpacity ^js react-native)))
(def touchable-highlight-class (reagent/adapt-react-class (.-TouchableHighlight ^js react-native)))
(def touchable-without-feedback-class
  (reagent/adapt-react-class (.-TouchableWithoutFeedback ^js react-native)))

(defn pressable
  [props & children]
  (into [pressable-class (utils/custom-pressable-props props)] children))

(defn touchable-opacity
  [props & children]
  (into [touchable-opacity-class (utils/custom-pressable-props props)] children))

(defn touchable-highlight
  [props & children]
  (into [touchable-highlight-class (utils/custom-pressable-props props)] children))

(defn touchable-without-feedback
  [props & children]
  (into [touchable-without-feedback-class (utils/custom-pressable-props props)] children))

(def flat-list flat-list/flat-list)

(def section-list section-list/section-list)

(def activity-indicator (reagent/adapt-react-class (.-ActivityIndicator ^js react-native)))

(def modal (reagent/adapt-react-class (.-Modal ^js react-native)))
(def refresh-control (reagent/adapt-react-class (.-RefreshControl ^js react-native)))

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

(defn use-ref-atom
  [value]
  (let [ref (use-ref (atom value))]
    (.-current ^js ref)))

(defn get-js-deps
  [deps]
  (if deps
    (let [prev-state (use-ref-atom {:value false :deps nil})
          prev-deps  (:deps @prev-state)
          prev-value (:value @prev-state)]
      (if (and (not (nil? prev-deps)) (not= (count deps) (count prev-deps)))
        (throw (js/Error. "Hooks can't have a different number of dependencies across re-renders"))
        (if (not= deps prev-deps)
          (let [new-value (not prev-value)]
            (reset! prev-state {:value new-value
                                :deps  deps})
            #js [new-value])
          #js [prev-value])))
    js/undefined))

(defn use-effect
  {:deprecated
   "use-mount or use-unmount should be used, more here https://github.com/status-im/status-mobile/blob/develop/doc/ui-guidelines.md#effects"}
  ([handler]
   (use-effect handler nil))
  ([handler deps]
   (react/useEffect
    #(let [ret (handler)] (if (fn? ret) ret js/undefined))
    (get-js-deps deps))))

(defn use-mount
  [handler]
  (use-effect handler []))

(defn use-unmount
  [handler]
  (use-mount (fn [] handler)))

(defn use-callback
  ([handler]
   (use-callback handler []))
  ([handler deps]
   (react/useCallback handler (get-js-deps deps))))

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

(def linking (.-Linking react-native))

(defn open-url [link] (.openURL ^js linking link))
