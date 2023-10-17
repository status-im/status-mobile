(ns quo.components.common.not-implemented.view
  (:require
    [quo.components.common.not-implemented.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- view-internal
  [{:keys [blur? theme]}]
  [rn/text {:style (style/text blur? theme)}
   "not implemented"])

(def view (quo.theme/with-theme view-internal))
