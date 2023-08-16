(ns quo2.components.record-audio.record-audio.buttons.record-button
  (:require [quo2.components.record-audio.record-audio.style :as style]
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
       :accessibility-label :mic-button
       :icon-only?          true}
      :i/audio]]))
