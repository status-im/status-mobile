(ns status-im.ui.components.checkbox.view
  (:require [status-im.ui.components.checkbox.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]))

(defn checkbox
  "react/check-box is currently not available on iOS,
  use it once it is available on all platforms"
  [{:keys [on-value-change checked? accessibility-label
           disabled? style icon-style]
    :or   {accessibility-label :checkbox}}]
  [react/touchable-highlight
   (merge {:style               (merge
                                 styles/wrapper
                                 style)
           :accessibility-label accessibility-label}
          (when on-value-change
            {:on-press #(on-value-change (not checked?))}))
   (if checked?
     [icons/icon
      :tiny-icons/tiny-check {:container-style (styles/icon-check-container true)
                              :color colors/white}]
     [react/view {:style  (styles/icon-check-container false)}])])

(defn radio-button
  [{:keys [on-value-change checked? accessibility-label
           disabled? style icon-style]
    :or   {accessibility-label :radio-button}}]
  [react/touchable-highlight
   (merge {:style               (merge
                                 styles/wrapper
                                 style)
           :accessibility-label accessibility-label}
          (when on-value-change
            {:on-press #(on-value-change (not checked?))}))
   (if checked?
     [icons/icon :main-icons/check {:color colors/blue}]
     [react/view])])
