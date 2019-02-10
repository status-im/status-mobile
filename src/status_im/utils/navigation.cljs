(ns status-im.utils.navigation
  (:require [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.utils.platform :as platform]))

(def navigation-actions
  (when (not platform/desktop?)
    (.-NavigationActions js-dependencies/react-navigation)))

(def navigation-events
  (when (not platform/desktop?)
    (.-NavigationEvents js-dependencies/react-navigation)))

(def stack-actions
  (when (not platform/desktop?)
    (.-StackActions js-dependencies/react-navigation)))

(def navigator-ref (atom nil))

(defn set-navigator-ref [ref]
  (reset! navigator-ref ref))

(defn can-be-called? []
  (and @navigator-ref
       (not platform/desktop?)))

(defn navigate-to [route]
  (when (can-be-called?)
    (.dispatch
     @navigator-ref
     (.navigate
      navigation-actions
      #js {:routeName (name route)}))))

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
