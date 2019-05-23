(ns status-im.ui.components.animation
  (:require [status-im.ui.components.react :as react]))

(defn start
  ([anim] (.start ^js anim))
  ([anim callback] (.start ^js anim callback)))

(defn anim-loop [animation]
  (.loop ^js (react/animated) animation))

(defn interpolate [anim-value config]
  (.interpolate ^js anim-value (clj->js config)))

(defn timing [anim-value config]
  (.timing ^js (react/animated) anim-value (clj->js config)))

(defn spring [anim-value config]
  (.spring ^js (react/animated) anim-value (clj->js config)))

(defn decay [anim-value config]
  (.decay ^js (react/animated) anim-value (clj->js config)))

(defn anim-sequence [animations]
  (.sequence ^js (react/animated) (clj->js animations)))

(defn parallel [animations]
  (.parallel ^js (react/animated) (clj->js animations)))

(defn anim-delay [duration]
  (.delay ^js (react/animated) duration))

(defn event [config]
  (.event ^js (react/animated) (clj->js [nil, config])))

(defn add-listener [anim-value listener]
  (.addListener ^js anim-value listener))

(defn remove-all-listeners [anim-value]
  (.removeAllListeners ^js anim-value))

(defn stop-animation [anim-value]
  (.stopAnimation ^js anim-value))

(defn set-value [anim-value value]
  (.setValue ^js anim-value value))

(defn create-value [value]
  (js/ReactNative.Animated.Value. ^js value))

(defn x [value-xy]
  (.-x ^js value-xy))

(defn y [value-xy]
  (.-y ^js value-xy))

(defn get-layout [value-xy]
  (js->clj (.getLayout ^js value-xy)))

(defn easing [] js/ReactNative.Easing)
(defn easing-in [] (.-in ^js (easing)))
(defn easing-out [] (.-out ^js (easing)))

(defn cubic [] (.-cubic ^js (easing)))
