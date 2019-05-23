(ns status-im.utils.navigation
  (:require [status-im.react-native.js-dependencies :as js-dependencies]
            [status-im.utils.platform :as platform]))

(def navigation-actions
  (.-NavigationActions ^js js-dependencies/react-navigation))

(def navigation-events
  (.-NavigationEvents ^js js-dependencies/react-navigation))

(def stack-actions
  (.-StackActions ^js js-dependencies/react-navigation))

(def navigator-ref (atom nil))

(defn set-navigator-ref [ref]
  (reset! navigator-ref ref))

(defn can-be-called? []
  @navigator-ref)

(defn navigate-to [route params]
  (when (can-be-called?)
    (.dispatch
     ^js @navigator-ref
     (.navigate
      navigation-actions
      #js {:routeName (name route)
           :params    (clj->js params)}))))

(defn- navigate [params]
  (when (can-be-called?)
    (.navigate ^js navigation-actions (clj->js params))))

(defn navigate-reset [state]
  (when (can-be-called?)
    (let [state' (update state :actions #(mapv navigate %))]
      (.dispatch
       ^js @navigator-ref
       (.reset
        ^js stack-actions
        (clj->js state'))))))

(defn navigate-back []
  (when (can-be-called?)
    (.dispatch
     ^js @navigator-ref
     (.back ^js navigation-actions))))
