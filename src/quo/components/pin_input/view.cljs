(ns quo.components.pin-input.view
  (:require [quo.components.markdown.text :as text]
            [quo.components.pin-input.pin.view :as pin]
            [quo.foundations.colors :as colors]
            quo.theme
            [react-native.core :as rn]))

(defn view
  [{:keys [number-of-pins number-of-filled-pins error? info info-error?]
    :or   {number-of-pins 6 number-of-filled-pins 0}}]
  (let [theme (quo.theme/use-theme)]
    [rn/view {:style {:align-items :center}}
     [rn/view {:style {:flex-direction :row}}
      (for [i (range 1 (inc number-of-pins))]
        ^{:key i}
        [pin/view
         {:state (cond
                   error?                            :error
                   (<= i number-of-filled-pins)      :filled
                   (= i (inc number-of-filled-pins)) :active)}])]
     (when info
       [text/text
        {:style {:color (if (or error? info-error?)
                          (colors/theme-colors colors/danger-50 colors/danger-60 theme)
                          (colors/theme-colors colors/neutral-50 colors/neutral-40 theme))}
         :size  :paragraph-2}
        info])]))
