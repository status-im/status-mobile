(ns status-im.ui.screens.add-new.open-dapp.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.action-button.action-button :as action-button]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.common.common :as components]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar.view]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.contact.contact :as contact-view]
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.screens.add-new.styles :as add-new.styles]
            [status-im.ui.screens.add-new.open-dapp.styles :as styles]))

(defn- render-dapp [dapp]
  [contact-view/contact-view {:contact             dapp
                              :on-press            #(re-frame/dispatch [:navigate-to :dapp-description dapp])
                              :show-forward?       true
                              :accessibility-label :dapp-item}])

(views/defview open-dapp []
  (views/letsubs [dapps [:contacts/all-dapps]
                  url-text (atom nil)]
    [react/keyboard-avoiding-view styles/main-container
     [status-bar/status-bar]
     [toolbar.view/simple-toolbar (i18n/label :t/open-dapp)]
     [components/separator]
     [react/view add-new.styles/input-container
      [react/text-input {:on-change-text      #(reset! url-text %)
                         :on-submit-editing   #(re-frame/dispatch [:browser.ui/dapp-url-submitted @url-text])
                         :placeholder         (i18n/label :t/enter-url)
                         :auto-capitalize     :none
                         :auto-correct        false
                         :style               add-new.styles/input
                         :accessibility-label :dapp-url-input
                         :return-key-type     :go}]]
     [list/section-list {:sections                  dapps
                         :key-fn                    :dapp-url
                         :render-fn                 render-dapp
                         :default-separator?        true
                         :enableEmptySections       true
                         :keyboardShouldPersistTaps :always}]]))

(views/defview dapp-description []
  (views/letsubs [{:keys [name dapp-url description] :as dapp} [:get-screen-params]]
    [react/keyboard-avoiding-view styles/main-container
     [status-bar/status-bar]
     [toolbar.view/simple-toolbar]
     [react/view {:margin-top 24 :align-items :center}
      [chat-icon.screen/dapp-icon-browser dapp 56]
      [react/text {:style               styles/dapp-name
                   :accessibility-label :dapp-name-text}
       name]
      [react/text {:style styles/dapp}
       (i18n/label :t/dapp)]]
     [react/view {:margin-top 24}
      [action-button/action-button {:label               (i18n/label :t/open)
                                    :icon                :main-icons/address
                                    :icon-opts           {:color colors/blue}
                                    :accessibility-label :open-dapp-button
                                    :on-press            #(re-frame/dispatch [:browser.ui/open-dapp-button-pressed dapp-url])}]
      [components/separator {:margin-left 72}]]
     [react/view styles/description-container
      [react/i18n-text {:style styles/gray-label :key :description}]
      [react/text {:style (merge styles/black-label {:padding-top 18})}
       description]
      [components/separator {:margin-top 15}]
      [react/i18n-text {:style (merge styles/gray-label {:padding-top 18}) :key :url}]
      [react/text {:style (merge styles/black-label {:padding-top 14})}
       dapp-url]
      [components/separator {:margin-top 6}]]]))
