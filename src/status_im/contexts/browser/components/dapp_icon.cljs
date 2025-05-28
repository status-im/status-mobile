(ns status-im.contexts.browser.components.dapp-icon
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]))

(defn view
  [{:keys [dapp size background-color]}]
  (let [icon  (:icon dapp)
        image (:logo-url dapp)]
    (when (or icon image)
      [rn/view
       {:padding          4
        :border-radius    20
        :background-color background-color
        :overflow         :hidden}
       (cond
         icon
         [rn/view {:style {:transform [{:scale 0.8}]}}
          [quo/icon (:icon dapp)
           {:size  size
            :color colors/white-opa-80}]]

         image
         [fast-image/fast-image
          {:source (:logo-url dapp)
           :style  {:width size :height size :border-radius 20}}])])))
