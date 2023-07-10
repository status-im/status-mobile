(ns quo2.components.record-audio.record-audio.buttons.record-button
  (:require [quo2.components.icon :as icons]
            [quo2.components.record-audio.record-audio.style :as style]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn :refer [use-effect]]
            [react-native.reanimated :as reanimated]
            [quo2.components.buttons.button.view :as button]
            [quo2.components.record-audio.record-audio.helpers :as helpers]))

(defn f-record-button
  [recording? reviewing-audio?]
  (let [opacity        (reanimated/use-shared-value 1)
        show-animation #(helpers/set-value opacity 1)
        hide-animation #(helpers/set-value opacity 0)]
    (use-effect (fn []
                  (if (or @recording? @reviewing-audio?)
                    (hide-animation)
                    (show-animation)))
                [@recording? @reviewing-audio?])
    [reanimated/view {:style (style/record-button-container opacity)}
     [button/button
      {:type                :outline
       :size                32
       :width               32
       :accessibility-label :mic-button}
      [icons/icon :i/audio {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}]]]))
