(ns react-native.core
  (:require
    ["react" :as react]
    ["react-native" :as react-native]
    [promesa.core :as promesa]
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
  (let [props (cond-> props
                platform/ios?
                (dissoc :resize-method)
                (and (:style props) platform/ios?)
                (update :style dissoc :resize-method))]
    [image-native
     (if (string? source)
       (assoc props :source {:uri source})
       props)]))

(defn image-get-size
  [uri]
  (promesa/create (fn [res rej]
                    (.getSize ^js (.-Image ^js react-native)
                              uri
                              (fn [width height] (res [width height]))
                              rej))))
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
  {:deprecated "pressable should be used instead"}
  [props child]
  [touchable-without-feedback-class (utils/custom-pressable-props props) child])

(def flat-list flat-list/flat-list)

(def section-list section-list/section-list)

(def activity-indicator (reagent/adapt-react-class (.-ActivityIndicator ^js react-native)))

(def modal (reagent/adapt-react-class (.-Modal ^js react-native)))
(def refresh-control (reagent/adapt-react-class (.-RefreshControl ^js react-native)))

(def keyboard ^js (.-Keyboard ^js react-native))

(def dismiss-keyboard! #(.dismiss keyboard))

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

(def use-state react/useState)

(def use-ref react/useRef)

(def use-context react/useContext)

(defn use-ref-atom
  [value]
  (let [ref (use-ref (atom value))]
    (.-current ^js ref)))

(defn get-js-deps
  [deps]
  (if deps
    (if (empty? deps)
      #js [true]
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
            #js [prev-value]))))
    js/undefined))

(defn use-effect
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

(defn use-memo
  [handler deps]
  (react/useMemo handler (get-js-deps deps)))

(defn delay-render
  [content]
  (let [[render? set-render] (use-state false)]
    (use-mount
     (fn []
       (js/setTimeout #(set-render true) 0)))
    (when render?
      content)))

(def layout-animation (.-LayoutAnimation ^js react-native))
(def configure-next (.-configureNext ^js layout-animation))

(def layout-animation-presets
  {:ease-in-ease-out (-> ^js layout-animation .-Presets .-easeInEaseOut)
   :linear           (-> ^js layout-animation .-Presets .-linear)
   :spring           (-> ^js layout-animation .-Presets .-spring)})

(defn selectable-text-input-manager
  []
  (when (exists? (.-NativeModules ^js react-native))
    (.-RNSelectableTextInputManager ^js (.-NativeModules ^js react-native))))

(def linking (.-Linking react-native))

(defn open-url [link] (.openURL ^js linking link))
