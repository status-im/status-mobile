(ns quo2.components.dividers.divider-line.view
  (:require
    [quo2.components.dividers.divider-line.style :as style]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- view-internal
  [props]
  [rn/view {:style (style/divider-line props)}])

(def view (quo.theme/with-theme view-internal))
