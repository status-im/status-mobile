(ns quo.components.common.notification-dot.view
  (:require
    [quo.components.common.notification-dot.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn view
  [{:keys [customization-color style blur?]}]
  (let [theme (quo.theme/use-theme)]
    [rn/view
     {:accessibility-label :notification-dot
      :style               (merge
                            (style/notification-dot customization-color theme blur?)
                            style)}]))
