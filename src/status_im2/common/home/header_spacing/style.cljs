(ns status-im2.common.home.header-spacing.style
  (:require
    [react-native.safe-area :as safe-area]
    [status-im2.common.home.constants :as constants]
    status-im2.constants))

(defn header-spacing
  [testnet-enabled?]
  {:height (+ constants/header-height
              (safe-area/get-top)
              (when testnet-enabled? status-im2.constants/testnet-banner-height))})
