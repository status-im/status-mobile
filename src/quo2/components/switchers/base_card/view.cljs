(ns quo2.components.switchers.base-card.view
  (:require [quo.react-native :as rn]
            [quo2.components.buttons.button.view :as button]
            [quo2.components.switchers.base-card.style :as style]
            [quo2.foundations.colors :as colors]))

(defn base-card
  [{:keys [banner on-press on-close customization-color]} & children]
  (let [card-ref (atom nil)
        color-50 (colors/custom-color customization-color 50)]
    [rn/touchable-opacity
     {:on-press       on-press
      :ref            #(reset! card-ref %)
      :active-opacity 1}
     [rn/view {:style (style/base-container color-50)}
      (when banner
        [rn/image
         {:source (:source banner)
          :style  {:width 160}}])
      [button/button
       {:size                      24
        :type                      :photo-bg
        :icon                      true
        :on-press                  on-close
        :override-background-color colors/neutral-80-opa-40
        :style                     style/close-button}
       :i/close]
      (into [rn/view {:style style/thumb-card}] children)]]))
