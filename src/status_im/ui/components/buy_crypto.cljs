(ns status-im.ui.components.buy-crypto
  (:require [quo.react-native :as rn]
            [quo.design-system.colors :as colors]
            [quo.core :as quo]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.i18n :as i18n]))

(defn banner [{:keys [on-close on-open]}]
  [rn/view {:style {:border-radius    16
                    :background-color (colors/get-color :interactive-02)
                    :flex-direction   :row
                    :justify-content  :space-between}}
   [rn/touchable-opacity {:style    {:padding-horizontal 8
                                     :padding-vertical   10
                                     :flex               1
                                     :flex-direction     :row
                                     :align-items        :center}
                          :on-press on-open}
    [rn/image {:source (resources/get-image :empty-wallet)
               :style  {:width  40
                        :height 40}}]
    [rn/view {:style {:padding-left 16
                      :flex         1}}
     [quo/text {:weight          :bold
                :number-of-lines 1
                :ellipsize-mode  :tail}
      (i18n/label :t/buy-crypto-title)]
     [quo/text {:color :link}
      (i18n/label :t/buy-crypto-description)
      " →"]]]
   [rn/touchable-opacity {:style    {:padding 4}
                          :on-press on-close}
    [vector-icons/icon :main-icons/close-circle {:color (colors/get-color :icon-02)}]]])
