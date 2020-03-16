(ns status-im.network.ui.network-details.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.react :as react]
            [status-im.i18n :as i18n]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.common.common :as components.common]
            [status-im.network.ui.styles :as st]
            [status-im.network.core :as network]
            [status-im.network.ui.views :as network-settings]
            [status-im.ui.components.topbar :as topbar]))

(views/defview network-details []
  (views/letsubs [{:keys [networks/selected-network]} [:get-screen-params]
                  current-network   [:networks/current-network]
                  networks          [:get-networks]]
    (let [{:keys [id name config]} selected-network
          connected?               (= id current-network)
          custom?                  (seq (filter #(= (:id %) id) (:custom networks)))]
      [react/view st/container
       [react/view components.styles/flex
        [topbar/topbar {:title :t/network-details}]
        [react/view components.styles/flex
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
           [react/view components.styles/flex
            [components.common/button {:label        (i18n/label :t/delete)
                                       :button-style st/delete-button
                                       :label-style  st/delete-button-text
                                       :on-press     #(re-frame/dispatch [::network/delete-network-pressed id])}]]])]])))
