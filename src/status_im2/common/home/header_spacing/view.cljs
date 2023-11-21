(ns status-im2.common.home.header-spacing.view
  (:require
    [react-native.core :as rn]
    [status-im2.common.home.header-spacing.style :as style]))

(defn view
  []
  [rn/view {:style (style/header-spacing)}])
