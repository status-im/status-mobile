(ns status-im.ui.components.checkbox.view
  (:require [status-im.ui.components.checkbox.styles :as styles]
            [status-im.ui.components.react :as react]))

;; TODO(jeluard) Migrate to native checkbox provided by RN 0.49
;; https://facebook.github.io/react-native/docs/checkbox.html

(defn checkbox [{:keys [on-value-change checked?]}]
  [react/touchable-highlight {:style styles/wrapper :on-press #(do (when on-value-change (on-value-change (not checked?))))}
   [react/view (styles/icon-check-container checked?)
    (when checked?
      [react/icon :check_on styles/check-icon])]])