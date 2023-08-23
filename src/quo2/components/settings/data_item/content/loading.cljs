(ns quo2.components.settings.data-item.content.loading
  (:require [react-native.core :as rn]
            [quo2.components.settings.data-item.style :as style]))

(defn view
  [{:keys [size blur? theme]}]
  [rn/view {:style (style/loading-container size blur? theme)}])
