(ns quo.components.selectors.reactions-selector.view
  (:require
    [quo.components.selectors.reaction-resource :as reactions.resource]
    [quo.components.selectors.reactions-selector.style :as style]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn view
  [{:keys [start-pressed?]}]
  (let [pressed? (reagent/atom start-pressed?)]
    (fn [{:keys [emoji container-style on-press
                 accessibility-label]
          :or   {accessibility-label :reaction}}]
      [rn/pressable
       {:accessibility-label     accessibility-label
        :allow-multiple-presses? true
        :style                   (merge (style/container @pressed?)
                                        container-style)
        :on-press                (fn [e]
                                   (swap! pressed? not)
                                   (when on-press
                                     (on-press e)))}
       [rn/text
        (reactions.resource/system-emojis emoji)]])))
