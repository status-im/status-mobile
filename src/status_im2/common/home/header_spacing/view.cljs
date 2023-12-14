(ns status-im2.common.home.header-spacing.view
  (:require
    [react-native.core :as rn]
    [status-im2.common.home.header-spacing.style :as style]
    [utils.re-frame :as rf]))

(defn view
  []
  (let [testnet-enabled? (rf/sub [:profile/testnet-enabled?])]
    [rn/view {:style (style/header-spacing testnet-enabled?)}]))
