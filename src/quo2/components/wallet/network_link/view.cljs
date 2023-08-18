(ns quo2.components.wallet.network-link.view
  (:require [react-native.core :as rn]
            [status-im2.common.resources :as resources]))

(defn view
  []
  [rn/image {:source (resources/get-image :network-link-1x-light)
             :style {:width 73
                     :height 66}}])
