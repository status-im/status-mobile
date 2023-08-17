(ns quo2.components.common.notification-dot.view
  (:require [react-native.core :as rn]
            [quo2.components.common.notification-dot.style :as style]
            [quo2.theme :as quo.theme]))

(defn view-internal
  [{:keys [customization-color style theme blur?]}]
  [rn/view
   {:accessibility-label :notification-dot
    :style               (merge
                          (style/notification-dot customization-color theme blur?)
                          style)}])

(def view (quo.theme/with-theme view-internal))
