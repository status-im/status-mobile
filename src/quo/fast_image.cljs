(ns quo.fast-image
  (:require [reagent.core :as reagent]
            ["react-native-fast-image" :as react-native-fast-image]))

;; Fast iamge

(def image (reagent/adapt-react-class react-native-fast-image))

(def fast-image-preload (.-preload react-native-fast-image))

(def fast-image-priorities
  {:low    (-> ^js react-native-fast-image .-priority .-low)
   :normal (-> ^js react-native-fast-image .-priority .-normal)
   :high   (-> ^js react-native-fast-image .-priority .-high)})

(def fast-image-cache
  {:immutable  (-> ^js react-native-fast-image .-cacheControl .-immutable)
   :web        (-> ^js react-native-fast-image .-cacheControl .-web)
   :cache-only (-> ^js react-native-fast-image .-cacheControl .-cacheOnly)})

(def fast-image-resize-mode
  {:contain (-> ^js react-native-fast-image .-resizeMode .-contain)
   :cover   (-> ^js react-native-fast-image .-resizeMode .-cover)
   :stretch (-> ^js react-native-fast-image .-resizeMode .-stretch)
   :center  (-> ^js react-native-fast-image .-resizeMode .-center)})
