(ns status-im.network-settings.screens.network-details
  (:require-macros [status-im.utils.views :refer [defview]])
  (:require
    [re-frame.core :refer [dispatch]]
    [status-im.components.status-bar :refer [status-bar]]
    [status-im.components.toolbar-new.view :refer [toolbar]]
    [status-im.components.context-menu :refer [context-menu]]
    [status-im.components.text-input-with-label.view :refer [text-input-with-label]]
    [status-im.network-settings.screen :refer [network-badge]]
    [status-im.components.react :refer [view text text-input icon touchable-highlight]]
    [status-im.components.sticky-button :refer [sticky-button]]
    [status-im.utils.platform :refer [platform-specific]]
    [status-im.i18n :as i18n]
    [clojure.string :as str]
    [status-im.network-settings.styles :as st]))

(def options
  [{:text (i18n/label :t/add-json-file)      :value #(dispatch [:network-add-json-file])}
   {:text (i18n/label :t/paste-json-as-text) :value #(dispatch [:network-paste-json-as-text])}
   {:text (i18n/label :t/:edit-rpc-url)      :value #(dispatch [:network-edit-rpc-url])}
   {:text (i18n/label :t/:remove-network)    :value #(dispatch [:network-remove])}])

(defview network-details []
  [{:keys [id name config]} [:get :selected-network]
   {:keys [network]} [:get-current-account]]
  (let [connected? (= id network)]
    [view {:flex 1}
     [status-bar]
     [toolbar]
     [network-badge {:name       name
                     :connected? connected?
                     :options    options}]
     (when-not connected?
       [touchable-highlight {:on-press #(dispatch [:connect-network id])}
        [view st/connect-button-container
         [view st/connect-button
          [text {:style st/connect-button-label
                 :uppercase? (get-in platform-specific [:uppercase?])}
           (i18n/label :t/connect)]]
         [text {:style st/connect-button-description}
          (i18n/label :t/connecting-requires-login)]]])
     [view st/network-config-container
      [text {:style st/network-config-text}
       config]]
     [view {:opacity 0.4}
      [view st/edit-button-container
        [view st/edit-button
         [text {:style st/edit-button-label
                :uppercase? (get-in platform-specific [:uppercase?])}
          (i18n/label :t/edit-network-config)]]
       #_[context-menu ; TODO should be implemented later
          [view st/edit-button
           [text {:style st/edit-button-label} (i18n/label :t/edit-network-config)]]
          options]
       [text {:style st/edit-button-description}
        (i18n/label :t/edit-network-warning)]]]]))