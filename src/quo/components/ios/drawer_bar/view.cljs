(ns quo.components.ios.drawer-bar.view
  (:require
    [quo.components.ios.drawer-bar.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- view-internal
  [props]
  [rn/view {:style style/handle-container}
   [rn/view {:style (style/handle props)}]])

(def view (quo.theme/with-theme view-internal))
