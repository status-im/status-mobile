(ns quo.components.common.notification-dot.view
  (:require
    [quo.components.common.notification-dot.style :as style]
    [quo.context :as quo.context]
    [react-native.core :as rn]))

(defn view
  [{:keys [customization-color style blur?]}]
  (let [theme (quo.context/use-theme)]
    [rn/view
     {:accessibility-label :notification-dot
      :style               (merge
                            (style/notification-dot customization-color theme blur?)
                            style)}]))
