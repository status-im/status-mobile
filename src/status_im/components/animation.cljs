(ns status-im.components.animation
  (:require [status-im.components.react :refer [animated]]))

(defn start
  ([anim] (.start anim))
  ([anim callback] (.start anim callback)))

(defn timing [anim-value config]
  (.timing animated anim-value (clj->js config)))

(defn spring [anim-value config]
  (.spring animated anim-value (clj->js config)))

(defn decay [anim-value config]
  (.decay animated anim-value (clj->js config)))

(defn anim-sequence [animations]
  (.sequence animated (clj->js animations)))

(defn parallel [animations]
  (.parallel animated (clj->js animations)))

(defn anim-delay [duration]
  (.delay animated duration))

(defn event [config]
  (.event animated (clj->js [nil, config])))

(defn add-listener [anim-value listener]
  (.addListener anim-value listener))

(defn remove-all-listeners [anim-value]
  (.removeAllListeners anim-value))

(defn stop-animation [anim-value]
  (.stopAnimation anim-value))

(defn value [anim-value]
  (.-value anim-value))

(defn set-value [anim-value value]
  (.setValue anim-value value))

(defn create-value [value]
  (js/ReactNative.Animated.Value. value))

(defn x [value-xy]
  (.-x value-xy))

(defn y [value-xy]
  (.-y value-xy))

(defn get-layout [value-xy]
  (js->clj (.getLayout value-xy)))

(defn easing [] js/ReactNative.Easing)