(ns quo.components.common.not-implemented.view
  (:require
    [quo.components.common.not-implemented.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn view
  [{:keys [blur?]}]
  (let [theme (quo.theme/use-theme)]
    [rn/text {:style (style/text blur? theme)}
     "not implemented"]))
