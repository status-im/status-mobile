(ns status-im.ui.screens.routing.core
  (:require
   [reagent.core :as reagent]
   [status-im.ui.components.react :as react]
   [re-frame.core :as re-frame]
   [taoensso.timbre :as log]
   [status-im.utils.platform :as platform]
   [oops.core :refer [ocall oget]]
   [status-im.react-native.js-dependencies :as js-dependencies]))

(defonce native js-dependencies/react-navigation-native)
(defonce stack  js-dependencies/react-navigation-stack)
(defonce bottom-tabs js-dependencies/react-navigation-bottom-tabs)

(def navigation-container (reagent/adapt-react-class
                           (oget native "NavigationContainer")))

(def use-focus-effect (oget native "useFocusEffect"))
(def use-callback (oget js-dependencies/react "useCallback"))

(def add-back-handler-listener (oget js-dependencies/back-handler "addEventListener"))
(def remove-back-handler-listener (oget js-dependencies/back-handler "removeEventListener"))

(def transition-presets (oget stack "TransitionPresets"))

(def modal-presentation-ios (merge (js->clj (oget transition-presets "ModalPresentationIOS"))
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
        (add-back-handler-listener "hardwareBackPress" on-back-press)
        (fn []
          (remove-back-handler-listener "hardwareBackPress" on-back-press))))
    #js [])))

(defn wrapped-screen-style [{:keys [insets]} insets-obj]
  (merge
   {:background-color :white
    :flex             1}
   (when (get insets :bottom)
     {:padding-bottom (oget insets-obj "bottom")})
   (when (get insets :top true)
     {:padding-top (oget insets-obj "top")})))

(defn presentation-type [{:keys [transition] :as opts}]
  (if (and platform/ios? (= transition :presentation-ios))
    (-> opts
        (update :options merge modal-presentation-ios)
        (assoc-in [:insets :top] false))
    opts))

(defn wrap-screen [{:keys [component] :as options}]
  (assoc options :component
         (fn [props]
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
  (let [nav-obj (ocall stack "createStackNavigator")]
    (get-navigator nav-obj)))

(defn create-bottom-tabs []
  (let [nav-obj (ocall bottom-tabs "createBottomTabNavigator")]
    (get-navigator nav-obj)))

(def common-actions (oget native "CommonActions"))
(def stack-actions (oget native "StackActions"))

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
           (ocall stack-actions "replace"
                  #js {:name   (name route)
                       :params (clj->js params)}))))
