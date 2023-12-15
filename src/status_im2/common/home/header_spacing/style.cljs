(ns status-im2.common.home.header-spacing.style
  (:require
    [react-native.safe-area :as safe-area]
    [status-im2.common.home.constants :as constants]))

(defn header-spacing
  []
  {:height (+ constants/header-height (safe-area/get-top))})
