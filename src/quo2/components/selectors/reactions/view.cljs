(ns quo2.components.selectors.reactions.view
  (:require [quo2.components.reactions.resource :as reactions.resource]
            [quo2.components.selectors.reactions.style :as style]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

(defn view
  [_ {:keys [start-pressed?]}]
  (let [pressed? (reagent/atom start-pressed?)]
    (fn [emoji
         {:keys [container-style on-press
                 accessibility-label]
          :or   {accessibility-label :reaction}}]
      [rn/touchable-without-feedback
       {:accessibility-label accessibility-label
        :on-press            (fn [e]
                               (swap! pressed? not)
                               (when on-press
                                 (on-press e)))}
       [rn/view
        {:style (merge (style/container @pressed?)
                       container-style)}
        [rn/image
         {:source (reactions.resource/get-reaction emoji)
          :style  {:width 20 :height 20}}]]])))
