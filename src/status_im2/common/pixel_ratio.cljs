(ns status-im2.common.pixel-ratio
  (:require ["react-native" :as rn]))

(def ratio (rn/PixelRatio.get))
