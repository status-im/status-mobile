(ns quo.components.browser.dapp-favorites.view
  (:require [quo.components.browser.dapp-favorites.style :as style]
            [quo.components.browser.dapp-item.view :as dapp-item]
            [react-native.core :as rn]))

(defn render-item
  [props]
  [dapp-item/view props])

(defn view
  [{:keys [dapps]}]
  [rn/view {:style style/container}
   [rn/flat-list
    {:flex                              1
     :horizontal                        true
     :data                              dapps
     :render-fn                         render-item
     :key-fn                            str
     :shows-horizontal-scroll-indicator false
     :content-container-style           style/content-container}]])
