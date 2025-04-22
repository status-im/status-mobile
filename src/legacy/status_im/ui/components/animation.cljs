(ns legacy.status-im.ui.components.animation
  (:require
    ["react-native" :as rn]
    [legacy.status-im.ui.components.react :as react]))

(defn start
  ([^js anim] (.start anim))
  ([^js anim callback] (.start anim callback)))

(defn anim-loop
  [animation]
  (.loop ^js react/animated animation))

(defn interpolate
  [^js anim-value config]
  (.interpolate anim-value (clj->js config)))

(defn add-native-driver
  [{:keys [useNativeDriver] :as config}]
  (assoc config
         :useNativeDriver
         (if (nil? useNativeDriver)
           true
           useNativeDriver)))

(defn timing
  [anim-value config]
  (.timing ^js react/animated
           anim-value
           (clj->js (add-native-driver config))))

(defn spring
  [anim-value config]
  (.spring ^js react/animated
           anim-value
           (clj->js (add-native-driver config))))

(defn anim-sequence
  [animations]
  (.sequence ^js react/animated (clj->js animations)))

(defn parallel
  [animations]
  (.parallel ^js react/animated (clj->js animations)))

(def animated-value (-> ^js rn .-Animated .-Value))
(def easing (-> ^js rn .-Easing))

(defn create-value
  [value]
  (new animated-value value))

(defn easing-in [] (.-in ^js easing))
(defn easing-out [] (.-out ^js easing))

