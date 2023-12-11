(ns quo.components.wallet.progress-bar.view
  (:require
    [quo.components.wallet.progress-bar.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- view-internal
  [{:keys [full-width?] :as props}]
  [rn/view
   {:accessibility-label :progress-bar
    :style               (style/root-container props)}
   (when full-width?
     [rn/view {:style (style/progressed-bar props)}])])

(def view (quo.theme/with-theme view-internal))
