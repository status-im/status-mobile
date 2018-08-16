(ns status-im.ui.screens.privacy-policy.views
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.screens.privacy-policy.styles :as styles]
            [status-im.i18n :as i18n]
            [re-frame.core :as re-frame]))

(defn privacy-policy-button []
  [react/touchable-highlight
   {:on-press #(re-frame/dispatch [:open-privacy-policy-link])}
   [react/view styles/privacy-policy-button-container
    [react/text {:style styles/privacy-policy-button-text-gray}
     (i18n/label :t/agree-by-continuing)
     [react/text
      {:style styles/privacy-policy-button-text}
      (i18n/label :t/privacy-policy)]]]])
