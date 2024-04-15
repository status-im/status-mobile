(ns quo.components.selectors.reactions-selector.view
  (:require
    [quo.components.selectors.reaction-resource :as reactions.resource]
    [quo.components.selectors.reactions-selector.style :as style]
    [react-native.core :as rn]))

(defn view
  [{:keys [emoji container-style on-press
           accessibility-label start-pressed?]
    :or   {accessibility-label :reaction}}]
  (let [[pressed? set-pressed] (rn/use-state start-pressed?)
        on-press               (fn [e]
                                 (set-pressed (not pressed?))
                                 (when on-press (on-press e)))]
    [rn/pressable
     {:accessibility-label     accessibility-label
      :allow-multiple-presses? true
      :style                   (merge (style/container pressed?)
                                      container-style)
      :on-press                on-press}
     [rn/text (reactions.resource/system-emojis emoji)]]))
