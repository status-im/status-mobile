(ns quo2.components.wallet.network-amount
  (:require [clojure.string :as string]
            [quo.react-native :as rn]
            [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [status-im.i18n.i18n :as i18n]))

(defn network-amount
  "[network-amount opts]
   opts
   {:style        style                 ;; extra styles for the container (takes precedence)
    :network-name \"Mainnet\"             ;; shown network name
    :icon         :main-icons2/ethereum ;; key of icon belonging to the network
    :eth-value    1.2345678             ;; shown ETH value}"
  [{:keys [show-right-border? style network-name icon eth-value] :as _opts}]
  [rn/view (merge {:accessibility-label :network-amount
                   :background-color    (colors/theme-colors colors/white colors/neutral-95)
                   :border-radius       16
                   :padding             6}
                  style)
   [rn/view {:style {:flex-direction :row
                     :align-items    :center}}
    [rn/view {:flex-direction :column
              :align-self     :flex-start
              :padding-top    3
              :padding-right  4}
     [icon/icon icon {:size            40
                      :no-color        true
                      :container-style {:width  12
                                        :height 12}}]]
    [rn/view
     [rn/view {:style {:flex-direction :row
                       :align-items    :center}}
      [text/text {:weight :medium
                  :size   :paragraph-2
                  :style  {:color (colors/theme-colors colors/neutral-100 colors/white)}}
       eth-value \space (i18n/label :t/eth)]
      [rn/view {:style {:border-right-width (when show-right-border? 1)
                        :border-right-color (colors/theme-colors colors/neutral-40 colors/neutral-50)
                        :padding-left       8
                        :align-self         :center
                        :height             8}}]]
     [text/text {:weight :medium
                 :size   :label
                 :style  {:color colors/neutral-50}}
      (string/lower-case (i18n/label :t/on)) \space network-name]]]])
