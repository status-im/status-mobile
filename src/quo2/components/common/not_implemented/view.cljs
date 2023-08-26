(ns quo2.components.common.not-implemented.view
  (:require [react-native.core :as rn]
            [quo2.theme :as quo.theme]
            [quo2.components.common.not-implemented.style :as style]))

(defn- view-internal
  [{:keys [blur? theme]}]
  [rn/text {:style (style/text blur? theme)}
   "not implemented"])

(def view (quo.theme/with-theme view-internal))
