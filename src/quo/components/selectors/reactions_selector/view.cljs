(ns quo.components.selectors.reactions-selector.view
  (:require
    [quo.components.selectors.reaction-resource :as reactions.resource]
    [quo.components.selectors.reactions-selector.style :as style]
    [quo.theme]
    [react-native.core :as rn]))

(defn view
  [{:keys [emoji container-style on-press
           accessibility-label start-pressed?]
    :or   {accessibility-label :reaction}}]
  (let [[pressed? set-pressed] (rn/use-state start-pressed?)
        theme                  (quo.theme/use-theme)
        on-press               (fn [e]
                                 (set-pressed (not pressed?))
                                 (when on-press (on-press e)))]
    [rn/pressable
     {:accessibility-label     accessibility-label
      :allow-multiple-presses? true
      :style                   (merge (style/container pressed? theme)
                                      container-style)
      :on-press                on-press}
     [rn/text
      {:style style/emoji-text-style}
      (reactions.resource/system-emojis emoji)]]))
