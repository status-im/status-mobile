(ns status-im.utils.pixel-ratio
  (:require ["react-native" :as rn]))

(def ratio (rn/PixelRatio.get))
