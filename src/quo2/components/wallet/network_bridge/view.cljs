(ns quo2.components.wallet.network-bridge.view
  (:require
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as quo.theme]
    [react-native.core :as rn]))



(defn network-bridge-internal
  []
  (let [network :ethereum]
    [rn/view {:style {:width              136
                      :height             44
                      :border-width       1
                      :border-radius      12
                      :padding-vertical   5
                      :padding-horizontal 8
                      :border-color       (get colors/networks network)}} [text/text {:size   :paragraph-2
                                                                                      :weight :medium} "50 SNT"]]))

(def network-bridge (quo.theme/with-theme network-bridge-internal))
