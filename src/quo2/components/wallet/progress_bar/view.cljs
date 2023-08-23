(ns quo2.components.wallet.progress-bar.view
  (:require [quo2.components.wallet.progress-bar.style :as style]
            [quo2.theme :as quo.theme]
            [react-native.core :as rn]))

(defn- view-internal
  [props]
  [rn/view
   {:accessibility-label :progress-bar
    :style               (style/root-container props)}])

(def view (quo.theme/with-theme view-internal))
