(ns status-im.ui.screens.network.network-details.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [quo.core :as quo]
            [status-im.network.core :as network]
            [status-im.ui.screens.network.styles :as st]
            [status-im.ui.screens.network.views :as network-settings]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar])
  (:require-macros [status-im.utils.views :as views]))

(views/defview network-details []
  (views/letsubs [{:keys [networks/selected-network]} [:get-screen-params]
                  current-network   [:networks/current-network]
                  networks          [:get-networks]]
    (let [{:keys [id name config]} selected-network
          connected?               (= id current-network)
          custom?                  (seq (filter #(= (:id %) id) (:custom networks)))]
      [react/view st/container
       [react/view {:flex 1}
        [topbar/topbar {:title (i18n/label :t/network-details)}]
        [react/view {:flex 1}
         [network-settings/network-badge
          {:name       name
           :connected? connected?}]
         (when-not connected?
           [react/touchable-highlight {:on-press #(re-frame/dispatch [::network/connect-network-pressed id])}
            [react/view st/connect-button-container
             [react/view {:style               st/connect-button
                          :accessibility-label :network-connect-button}
              [react/text {:style st/connect-button-label}
               (i18n/label :t/connect)]]
             [react/i18n-text {:style st/connect-button-description
                               :key   :connecting-requires-login}]]])
         [react/view (st/network-config-container)
          [react/text {:style               st/network-config-text
                       :accessibility-label :network-details-text}
           config]]]
        (when custom?
          [react/view st/bottom-container
           [react/view {:flex 1}
            [quo/button {:on-press    #(re-frame/dispatch [::network/delete-network-pressed id])}
             (i18n/label :t/delete)]]])]])))
