(ns legacy.status-im.ui.screens.network.network-details.views
  (:require
    [legacy.status-im.network.core :as network]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.screens.network.styles :as st]
    [legacy.status-im.ui.screens.network.views :as network-settings]
    [re-frame.core :as re-frame]
    [utils.debounce :refer [dispatch-and-chill]]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :as views]))

(views/defview network-details
  []
  (views/letsubs [{:keys [networks/selected-network]} [:get-screen-params]
                  current-network                     [:networks/current-network]
                  networks                            [:get-networks]]
    (let [{:keys [id name config]} selected-network
          connected?               (= id current-network)
          custom?                  (seq (filter #(= (:id %) id) (:custom networks)))]
      [:<>
       [react/view {:flex 1}
        [network-settings/network-badge
         {:name       name
          :connected? connected?}]
        (when-not connected?
          [react/touchable-highlight
           {:on-press #(dispatch-and-chill [::network/connect-network-pressed id] 1000)}
           [react/view st/connect-button-container
            [react/view
             {:style               st/connect-button
              :accessibility-label :network-connect-button}
             [react/text {:style st/connect-button-label}
              (i18n/label :t/connect)]]
            [react/i18n-text
             {:style st/connect-button-description
              :key   :connecting-requires-login}]]])
        [react/view (st/network-config-container)
         [react/text
          {:style               st/network-config-text
           :accessibility-label :network-details-text}
          config]]]
       (when custom?
         [react/view st/bottom-container
          [react/view {:flex 1}
           [quo/button {:on-press #(re-frame/dispatch [::network/delete-network-pressed id])}
            (i18n/label :t/delete)]]])])))
