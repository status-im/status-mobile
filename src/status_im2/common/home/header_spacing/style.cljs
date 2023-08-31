(ns status-im2.common.home.header-spacing.style
  (:require [status-im2.common.home.constants :as constants]
            [react-native.safe-area :as safe-area]))

(defn header-spacing
  []
  {:height (+ constants/header-height (safe-area/get-top))})
