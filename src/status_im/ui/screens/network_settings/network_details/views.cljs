(ns status-im.ui.screens.network-settings.network-details.views
  (:require-macros [status-im.utils.views :as views])
  (:require
    [re-frame.core :as rf]
    [status-im.ui.components.status-bar.view :as status-bar]
    [status-im.ui.components.toolbar.view :as toolbar]
    [status-im.ui.screens.network-settings.views :as network-settings]
    [status-im.ui.components.react :as react]
    [status-im.utils.platform :as platform]
    [status-im.i18n :as i18n]
    [status-im.ui.screens.network-settings.styles :as st]))

(def options
  [{:text  (i18n/label :t/add-json-file)
    :value #(rf/dispatch [:network-add-json-file])}
   {:text  (i18n/label :t/paste-json-as-text)
    :value #(rf/dispatch [:network-paste-json-as-text])}
   {:text  (i18n/label :t/:edit-rpc-url)
    :value #(rf/dispatch [:network-edit-rpc-url])}
   {:text  (i18n/label :t/:remove-network)
    :value #(rf/dispatch [:network-remove])}])

(views/defview network-details []
  (views/letsubs [{:keys [networks/selected-network]} [:get-screen-params]
                  {:keys [network]} [:get-current-account]]
    (let [{:keys [id name config]} selected-network
          connected?               (= id network)]
      [react/view {:flex 1}
       [status-bar/status-bar]
       [toolbar/simple-toolbar]
       [network-settings/network-badge
        {:name       name
         :connected? connected?}]
       (when-not connected?
         [react/touchable-highlight {:on-press #(rf/dispatch [:connect-network id])}
          [react/view st/connect-button-container
           [react/view {:style               st/connect-button
                        :accessibility-label :network-connect-button}
            [react/text {:style      st/connect-button-label
                         :uppercase? (get-in platform/platform-specific [:uppercase?])}
             (i18n/label :t/connect)]]
           [react/text {:style st/connect-button-description}
            (i18n/label :t/connecting-requires-login)]]])
       [react/view st/network-config-container
        [react/text {:style               st/network-config-text
                     :accessibility-label :network-details-text}
         config]]
       ;; TODO(rasom): uncomment edit-button when it will be functional,
       ;; https://github.com/status-im/status-react/issues/2104
       #_[react/view {:opacity 0.4}
          [react/view st/edit-button-container
           [react/view st/edit-button
            [react/text {:style      st/edit-button-label
                         :uppercase? (get-in platform/platform-specific [:uppercase?])}
             (i18n/label :t/edit-network-config)]]
           #_[context-menu                                      ; TODO should be implemented later
              [view st/edit-button
               [text {:style st/edit-button-label} (i18n/label :t/edit-network-config)]]
              options]
           [react/text {:style st/edit-button-description}
            (i18n/label :t/edit-network-warning)]]]])))
