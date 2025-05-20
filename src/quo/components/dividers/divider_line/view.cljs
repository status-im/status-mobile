(ns quo.components.dividers.divider-line.view
  (:require
    [quo.components.dividers.divider-line.style :as style]
    [quo.context :as quo.context]
    [react-native.core :as rn]))

(defn view
  [props]
  (let [theme (quo.context/use-theme)]
    [rn/view {:style (style/divider-line props theme)}]))
