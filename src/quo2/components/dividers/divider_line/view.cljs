(ns quo2.components.dividers.divider-line.view
  (:require
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]
    [quo2.components.dividers.divider-line.style :as style]))

(defn- view-internal
  [{:keys [blur? theme]}]
  [rn/view {:style (style/divider-line blur? theme)}])

(def view (quo.theme/with-theme view-internal))
