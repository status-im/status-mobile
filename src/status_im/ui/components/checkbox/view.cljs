(ns status-im.ui.components.checkbox.view
  (:require [status-im.ui.components.checkbox.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]))

(defn- checkbox-generic [{:keys [on-value-change checked? accessibility-label
                                 disabled? style icon-style native?]
                          :or   {accessibility-label :checkbox
                                 native?             true}} plain?]
  (let [icon-check-container (if plain? #() styles/icon-check-container)
        check-icon           (merge (if plain? styles/plain-check-icon styles/check-icon) icon-style)]
    (if (and (or platform/android?
                 platform/desktop?)
             native?)
      [react/view (merge styles/wrapper style)
       [react/check-box {:on-value-change     on-value-change
                         :value               checked?
                         :disabled            (and (not checked?)
                                                   disabled?)
                         :accessibility-label accessibility-label}]]
      [react/touchable-highlight (merge {:style               (merge
                                                               (icon-check-container checked?)
                                                               styles/wrapper
                                                               style)
                                         :accessibility-label accessibility-label}
                                        (when on-value-change {:on-press #(on-value-change (not checked?))}))
       [react/view {}
        (when checked?
          [react/icon :check_on check-icon])]])))

(defn checkbox [props]
  [checkbox-generic props false])

(defn plain-checkbox [props]
  [checkbox-generic props true])
