(ns status-im.common.home.header-spacing.style
  (:require
    [react-native.safe-area :as safe-area]
    [status-im.common.home.constants :as constants]))

(defn header-spacing
  []
  {:height (+ constants/header-height safe-area/top)})
