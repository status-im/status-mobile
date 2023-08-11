(ns quo2.components.common.notification-dot.view
  (:require [react-native.core :as rn]
            [quo2.components.common.notification-dot.style :as style]))

(defn notification-dot
  [{:keys [customization-color style theme]}]
  [rn/view
   {:accessibility-label :notification-dot
    :style               (merge
                          (style/notification-dot customization-color theme)
                          style)}])
