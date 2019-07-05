(ns status-im.utils.navigation
  (:require [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.utils.platform :as platform]
            [goog.object :as gobj]))

(def navigation-actions
  (.-NavigationActions js-dependencies/react-navigation))

(def navigation-events
  (.-NavigationEvents js-dependencies/react-navigation))

(def stack-actions
  (.-StackActions js-dependencies/react-navigation))

(def navigator-ref (atom nil))

(defn set-navigator-ref [ref]
  (reset! navigator-ref ref))

(defn can-be-called? []
  @navigator-ref)

(defn navigate-to [route params]
  (when (can-be-called?)
    (.dispatch
     @navigator-ref
     (.navigate
      navigation-actions
      #js {:routeName (name route)
           :params    (clj->js params)}))))

(defn- navigate [params]
  (when (can-be-called?)
    (.navigate navigation-actions (clj->js params))))

(defn navigate-reset [state]
  (when (can-be-called?)
    (let [state' (update state :actions #(mapv navigate %))]
      (.dispatch
       @navigator-ref
       (.reset
        stack-actions
        (clj->js state'))))))

(defn navigate-back []
  (when (can-be-called?)
    (.dispatch
     @navigator-ref
     (.back navigation-actions))))

(defonce TwoPaneNavigator (gobj/get js-dependencies/react-native-navigation-twopane #js ["createTwoPaneNavigator"]))

(defn twopane-navigator [routeConfigs stackNavigatorConfig]
  (TwoPaneNavigator (clj->js routeConfigs) (clj->js stackNavigatorConfig)))
