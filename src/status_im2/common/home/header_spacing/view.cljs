(ns status-im2.common.home.header-spacing.view
  (:require [status-im2.common.home.header-spacing.style :as style]
            [react-native.core :as rn]))

(defn view
  []
  [rn/view {:style (style/header-spacing)}])
