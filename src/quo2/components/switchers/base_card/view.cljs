(ns quo2.components.switchers.base-card.view
  (:require [quo.react-native :as rn]
            [quo2.components.buttons.button.view :as button]
            [quo2.components.switchers.base-card.style :as style]))

(defn base-card
  [{:keys [banner on-press on-close customization-color]} & children]
  (let [card-ref (atom nil)]
    [rn/touchable-opacity
     {:on-press       on-press
      :ref            #(reset! card-ref %)
      :active-opacity 1}
     [rn/view {:style (style/base-container customization-color)}
      (when banner
        [rn/image
         {:source (:source banner)
          :style  {:width 160}}])
      [button/button
       {:size       24
        :type       :grey
        :icon       true
        :on-press   on-close
        :background :photo
        :style      style/close-button}
       :i/close]
      (into [rn/view {:style style/thumb-card}] children)]]))
