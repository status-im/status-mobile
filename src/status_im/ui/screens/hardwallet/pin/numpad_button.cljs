(ns status-im.ui.screens.hardwallet.pin.numpad-button
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.button.animated :as animated-button]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.screens.hardwallet.pin.styles :as styles]))

(defn numpad-button [n step enabled? small-screen?]
  [animated-button/button {:on-press #(when enabled?
                                        (re-frame/dispatch [:hardwallet.ui/pin-numpad-button-pressed n step]))
                           :duration 200
                           :style    (styles/numpad-button small-screen?)}
   [react/text {:style styles/numpad-button-text}
    n]])

(defn numpad-delete [step enabled? small-screen?]
  [animated-button/button
   {:duration 200
    :style    (styles/numpad-delete-button small-screen?)
    :on-press #(when enabled?
                 (re-frame/dispatch [:hardwallet.ui/pin-numpad-delete-button-pressed step]))}
   [vector-icons/icon :main-icons/backspace {:color colors/blue}]])
