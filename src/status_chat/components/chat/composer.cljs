(ns status-chat.components.chat.composer
  (:require [quo.context :as quo.context]
            [quo.core :as quo]
            [quo.foundations.colors :as quo.colors]
            [react-native.core :as rn]))

(defn container
  [{:keys [color]} & children]
  (let [theme (quo.context/use-theme)]
    (into [rn/view
           {:style {:border-radius      20
                    :padding-vertical   20
                    :padding-horizontal 16
                    :flex-direction     :row
                    :align-items        :center
                    :justify-content    :center
                    :background-color   (quo.colors/resolve-color color theme 30)}}]
          children)))

(defn submit-button
  [{:keys [on-submit disabled? color]}]
  [quo/button
   {:icon-only?          true
    :size                32
    :customization-color color
    :accessibility-label :onboarding-sumbit
    :disabled?           disabled?
    :on-press            on-submit}
   :i/arrow-up])
