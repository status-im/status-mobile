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

(defn decay
  [anim-value config]
  (.decay ^js react/animated
          anim-value
          (clj->js (add-native-driver config))))

(defn anim-sequence
  [animations]
  (.sequence ^js react/animated (clj->js animations)))

(defn parallel
  [animations]
  (.parallel ^js react/animated (clj->js animations)))

(defn anim-delay
  [duration]
  (.delay ^js react/animated duration))

(defn event
  [mapping config]
  (.event ^js react/animated (clj->js mapping) (clj->js config)))

(defn add-listener
  [^js anim-value listener]
  (.addListener anim-value listener))

(defn remove-all-listeners
  [^js anim-value]
  (.removeAllListeners anim-value))

(defn stop-animation
  [^js anim-value]
  (.stopAnimation anim-value))

(defn set-value
  [^js anim-value value]
  (.setValue anim-value value))

(def animated (.-Animated ^js rn))
(def animated-value (-> ^js rn .-Animated .-Value))
(def animated-value-xy (-> ^js rn .-Animated .-ValueXY))
(def easing (-> ^js rn .-Easing))

(defn create-value
  [value]
  (new animated-value value))

(defn create-value-xy
  [value]
  (new animated-value-xy value))

(defn add
  [anim-x anim-y]
  ((-> ^js rn .-Animated .add) anim-x anim-y))

(defn subtract
  [anim-x anim-y]
  ((-> ^js rn .-Animated .-subtract) anim-x anim-y))

(defn x
  [^js value-xy]
  (.-x value-xy))

(defn y
  [^js value-xy]
  (.-y value-xy))

(defn get-layout
  [^js value-xy]
  (js->clj (.getLayout value-xy)))

(defn easing-in [] (.-in ^js easing))
(defn easing-out [] (.-out ^js easing))

(defn cubic [] (.-cubic ^js easing))
(def bezier (.-bezier ^js easing))
