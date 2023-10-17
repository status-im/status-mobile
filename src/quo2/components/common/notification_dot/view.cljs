(ns quo2.components.common.notification-dot.view
  (:require
    [quo2.components.common.notification-dot.style :as style]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]))

(defn view-internal
  [{:keys [customization-color style theme blur?]}]
  [rn/view
   {:accessibility-label :notification-dot
    :style               (merge
                          (style/notification-dot customization-color theme blur?)
                          style)}])

(def view (quo.theme/with-theme view-internal))
