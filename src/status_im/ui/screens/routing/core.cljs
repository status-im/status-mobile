(ns status-im.ui.screens.routing.core
  (:require ["react" :refer (useCallback useEffect)]
            ["react-native" :refer (BackHandler)]
            ["@react-navigation/native" :refer (NavigationContainer StackActions CommonActions useFocusEffect) :as react-navigation]
            ["@react-navigation/stack" :refer (createStackNavigator TransitionPresets)]
            ["@react-navigation/bottom-tabs" :refer (createBottomTabNavigator)]
            [reagent.core :as reagent]
            [status-im.ui.components.colors :as colors]
            [re-frame.core :as re-frame]
            [taoensso.timbre :as log]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.react :as react]
            ;; NOTE(Ferossgp): This is temporary to mimic the behaviour of old input
            [quo.components.text-input :as quo]
            [oops.core :refer [ocall oget]]))

(def navigation-container (reagent/adapt-react-class NavigationContainer))

(def use-focus-effect useFocusEffect)
(def use-callback useCallback)
(def use-effect useEffect)

(defn add-back-handler-listener
  [callback]
  (.addEventListener BackHandler "hardwareBackPress" callback))

(defn remove-back-handler-listener
  [callback]
  (.removeEventListener BackHandler "hardwareBackPress" callback))

(def transition-presets TransitionPresets)

(def modal-presentation-ios (merge (js->clj (.-ModalPresentationIOS ^js transition-presets))
                                   {:gestureEnabled     true
                                    :cardOverlayEnabled true}))

;; TODO(Ferossgp): Unify with topbar back icon. Maybe dispatch the same event and move the all logic inside the event.
(defn handle-on-screen-focus
  [{:keys [back-handler on-focus name]}]
  (use-focus-effect
   (use-callback
    (fn []
      (log/debug :on-screen-focus name)
      (let [on-back-press (fn []
                            (when (and back-handler
                                       (not= back-handler :noop))
                              (re-frame/dispatch back-handler))
                            (boolean back-handler))]
        (when on-focus (re-frame/dispatch on-focus))
        (add-back-handler-listener on-back-press)
        (fn []
          (remove-back-handler-listener on-back-press))))
    #js [])))

(defn handle-on-screen-blur [navigation]
  (use-effect
   (fn []
     (ocall navigation "addListener" "blur"
            (fn []
              ;; Reset currently mounted text inputs to their default values
              ;; on navigating away; this is a privacy measure
              (println @quo/text-input-refs)
              (doseq [[_ {:keys [ref value]}] @quo/text-input-refs]
                (.setNativeProps ^js ref (clj->js {:text value})))
              (doseq [[^js text-input default-value] @react/text-input-refs]
                (.setNativeProps text-input (clj->js {:text default-value}))))))
   #js [navigation]))

(defn wrapped-screen-style [{:keys [insets style]} insets-obj]
  (merge
   {:background-color colors/white
    :flex             1}
   style
   (when (get insets :bottom)
     {:padding-bottom (+ (oget insets-obj "bottom")
                         (get style :padding-bottom)
                         (get style :padding-vertical))})
   (when (get insets :top true)
     {:padding-top (+ (oget insets-obj "top")
                      (get style :padding-top)
                      (get style :padding-vertical))})))

(defn presentation-type [{:keys [transition] :as opts}]
  (if (and platform/ios? (= transition :presentation-ios))
    (-> opts
        (update :options merge modal-presentation-ios)
        ;; NOTE: solution till https://github.com/react-navigation/react-navigation/pull/7943 is merged
        (update-in [:style :padding-bottom] + 10)
        (assoc-in [:insets :top] false))
    opts))

(defn wrap-screen [{:keys [component] :as options}]
  (assoc options :component
         (fn [props]
           (handle-on-screen-blur
            (oget props "navigation"))
           (handle-on-screen-focus options)
           (let [props'   (js->clj props :keywordize-keys true)
                 focused? (oget props "navigation" "isFocused")]
             (reagent/as-element
              [react/safe-area-consumer
               (fn [insets]
                 (reagent/as-element
                  [react/view {:style (wrapped-screen-style options insets)}
                   [component props' (focused?)]]))])))))

(defn- get-screen [navigator]
  (let [screen (reagent/adapt-react-class (oget navigator "Screen"))]
    (fn [props]
      [screen (-> props presentation-type wrap-screen)])))

(defn- get-navigator [nav-object]
  (let [navigator (reagent/adapt-react-class (oget nav-object "Navigator"))
        screen    (get-screen nav-object)]
    (fn [props children]
      (into [navigator props]
            (mapv screen children)))))

(defn create-stack []
  (let [nav-obj (createStackNavigator)]
    (get-navigator nav-obj)))

(defn create-bottom-tabs []
  (let [nav-obj (createBottomTabNavigator)]
    (get-navigator nav-obj)))

(def common-actions CommonActions)
(def stack-actions StackActions)

(defonce navigator-ref (reagent/atom nil))

(defn set-navigator-ref [ref]
  (reset! navigator-ref ref))

(defn can-be-called? []
  (boolean @navigator-ref))

(defn navigate-to [route params]
  (when (can-be-called?)
    (ocall @navigator-ref "dispatch"
           (ocall common-actions "navigate"
                  #js {:name   (name route)
                       :params (clj->js params)}))))

(defn navigate-reset [state]
  (when (can-be-called?)
    (ocall @navigator-ref "dispatch"
           (ocall common-actions "reset"
                  (clj->js state)))))

(defn navigate-back []
  (when (can-be-called?)
    (ocall @navigator-ref "dispatch"
           (ocall common-actions "goBack"))))

(defn navigate-replace [route params]
  (when (can-be-called?)
    (ocall @navigator-ref "dispatch"
           (ocall stack-actions "replace" (name route) (clj->js params)))))
