(ns quo.components.record-audio.record-audio.buttons.record-button
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.record-audio.record-audio.helpers :as helpers]
    [quo.components.record-audio.record-audio.style :as style]
    [react-native.core :as rn]
    [react-native.reanimated :as reanimated]))

(defn record-button
  [recording? reviewing-audio?]
  (let [opacity (reanimated/use-shared-value 1)]
    (rn/use-effect (fn []
                     (if (or recording? reviewing-audio?)
                       (helpers/set-value opacity 0)
                       (helpers/set-value opacity 1)))
                   [recording? reviewing-audio?])
    [reanimated/view {:style (style/record-button-container opacity)}
     [button/button
      {:type                :outline
       :size                32
       :accessibility-label :mic-button
       :icon-only?          true}
      :i/audio]]))
