(ns status-im2.contexts.wallet.common.account-subtitle.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]))

(defn view
  [{:keys [address theme networks]}]
  [quo/text {:size :paragraph-2}
   (map (fn [{:keys [short-name network-name]}]
          ^{:key (str network-name)}
          [quo/text
           {:size   :paragraph-2
            :weight :medium
            :style  {:color (colors/resolve-color network-name theme)}}
           (str short-name ":")])
        networks)
   [quo/text
    {:size   :paragraph-2
     :weight :monospace}
    address]])
