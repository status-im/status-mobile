(ns status-im.ui.components.animation
  (:require [status-im.ui.components.react :as react]))

(defn start
  ([anim] (.start anim))
  ([anim callback] (.start anim callback)))

(defn anim-loop [animation]
  (.loop (react/animated) animation))

(defn interpolate [anim-value config]
  (.interpolate anim-value (clj->js config)))

(defn add-native-driver [{:keys [useNativeDriver] :as config}]
  (assoc config
         :useNativeDriver
         (if (nil? useNativeDriver)
           true
           useNativeDriver)))

(defn timing [anim-value config]
  (.timing (react/animated)
           anim-value
           (clj->js (add-native-driver config))))

(defn spring [anim-value config]
  (.spring (react/animated)
           anim-value
           (clj->js (add-native-driver config))))

(defn decay [anim-value config]
  (.decay (react/animated)
          anim-value
          (clj->js (add-native-driver config))))

(defn anim-sequence [animations]
  (.sequence (react/animated) (clj->js animations)))

(defn parallel [animations]
  (.parallel (react/animated) (clj->js animations)))

(defn anim-delay [duration]
  (.delay (react/animated) duration))

(defn event [config]
  (.event (react/animated) (clj->js [nil, config])))

(defn add-listener [anim-value listener]
  (.addListener anim-value listener))

(defn remove-all-listeners [anim-value]
  (.removeAllListeners anim-value))

(defn stop-animation [anim-value]
  (.stopAnimation anim-value))

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
(defn easing-in [] (.-in (easing)))
(defn easing-out [] (.-out (easing)))

(defn cubic [] (.-cubic (easing)))
