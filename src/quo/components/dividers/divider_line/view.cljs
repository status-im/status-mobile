(ns quo.components.dividers.divider-line.view
  (:require
    [quo.components.dividers.divider-line.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn view
  [props]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style (style/divider-line props theme)}]))
