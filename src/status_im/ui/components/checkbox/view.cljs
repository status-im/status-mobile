(ns status-im.ui.components.checkbox.view
  (:require [status-im.ui.components.checkbox.styles :as styles]
            [status-im.ui.components.react :as react]
            [status-im.utils.platform :as platform]))

(defn- checkbox-generic [{:keys [on-value-change checked? accessibility-label style] :or {accessibility-label :checkbox}} plain?]
  (let [icon-check-container (if plain? #() styles/icon-check-container)
        check-icon           (if plain? styles/plain-check-icon styles/check-icon)]
    (if platform/android?
      [react/view (merge styles/wrapper style)
       [react/check-box {:on-value-change     on-value-change
                         :value               checked?
                         :accessibility-label accessibility-label}]]
      [react/touchable-highlight (merge {:style               (merge styles/wrapper style)
                                         :accessibility-label accessibility-label}
                                        (when on-value-change {:on-press #(on-value-change (not checked?))}))
       [react/view (icon-check-container checked?)
        (when checked?
          [react/icon :check_on check-icon])]])))

(defn checkbox [props]
  [checkbox-generic props false])

(defn plain-checkbox [props]
  [checkbox-generic props true])
