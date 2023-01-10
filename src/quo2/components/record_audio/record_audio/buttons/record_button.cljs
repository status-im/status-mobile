(ns quo2.components.record-audio.record-audio.buttons.record-button
  (:require [quo2.components.icon :as icons]
            [quo2.components.record-audio.record-audio.style :as style]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn :refer [use-effect]]
            [react-native.reanimated :as ra]
            [quo2.components.buttons.button :as button]
            [quo2.components.record-audio.record-audio.helpers :refer [set-value]]))

(defn record-button
  [recording? reviewing-audio?]
  [:f>
   (fn []
     (let [opacity        (ra/use-val 1)
           show-animation #(set-value opacity 1)
           hide-animation #(set-value opacity 0)]
       (use-effect (fn []
                     (if (or @recording? @reviewing-audio?)
                       (hide-animation)
                       (show-animation)))
                   [@recording? @reviewing-audio?])
       [ra/view {:style (style/record-button-container opacity)}
        [button/button
         {:type                :outline
          :size                32
          :width               32
          :accessibility-label :mic-button}
         [icons/icon :i/audio {:color (colors/theme-colors colors/neutral-100 colors/white)}]]]))])
