(ns status-im.ui.screens.desktop.main.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.screens.desktop.main.tabs.profile.views :as profile.views]
            [status-im.ui.screens.desktop.main.tabs.home.views :as home.views]
            [status-im.ui.screens.desktop.main.styles :as styles]
            [status-im.ui.screens.desktop.main.chat.views :as chat.views]
            [status-im.ui.screens.desktop.main.add-new.views :as add-new.views]
            [status-im.ui.screens.wallet.main.views :as wallet.main]
            [status-im.ui.components.desktop.tabs :as tabs]
            [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.ui.screens.wallet.onboarding.setup.views :as wallet.onboarding.setup]
            [status-im.ui.screens.wallet.send.views :as wallet.send]
            [status-im.ui.screens.wallet.request.views :as wallet.request]
            [status-im.ui.screens.wallet.transactions.views :as wallet.transactions]
            [status-im.ui.screens.wallet.components.views :as wallet.components]
            [status-im.ui.screens.wallet.transaction-sent.views :as wallet.transaction-sent]
            [status-im.ui.screens.wallet.transaction-fee.views :as wallet.transaction-fee]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.wallet.settings.views :as wallet-settings]))

(views/defview status-view []
  [react/view {:style {:flex 1 :background-color "#eef2f5" :align-items :center :justify-content :center}}
   [react/text {:style {:font-size 18 :color "#939ba1"}}
    "Status.im"]])

(views/defview tab-views []
  (views/letsubs [tab [:get-in [:desktop/desktop :tab-view-id]]]
    (let [component (case tab
                      :profile profile.views/profile-data
                      :home home.views/chat-list-view-wrapper
                      :wallet react/view
                      react/view)]
      [react/view {:style {:flex 1}}
       [component]])))

(views/defview main-view []
  (views/letsubs [view-id [:get :view-id]]
    (let [component (case view-id
                      :chat chat.views/chat-view
                      :new-contact add-new.views/new-contact
                      :qr-code profile.views/qr-code
                      :advanced-settings profile.views/advanced-settings
                      :chat-profile chat.views/chat-profile
                      :backup-recovery-phrase profile.views/backup-recovery-phrase
                      :wallet wallet.main/wallet
                      :wallet-settings-assets wallet-settings/manage-assets
                      :wallet-onboarding-setup wallet.onboarding.setup/screen
                      :wallet-send-transaction wallet.send/send-transaction
                      :recent-recipients       wallet.components/recent-recipients
                      :contact-code                 wallet.components/contact-code
                      :wallet-send-assets wallet.components/send-assets
                      :wallet-transaction-fee wallet.transaction-fee/transaction-fee
                      :wallet-transaction-sent wallet.transaction-sent/transaction-sent
                      :wallet-request-transaction wallet.request/request-transaction
                      :wallet-request-assets wallet.components/request-assets
                      :wallet-send-transaction-request wallet.request/send-transaction-request
                      :transactions-history wallet.transactions/transactions
                      :wallet-transaction-details   wallet.transactions/transaction-details
                      status-view)]
      [react/view {:style {:flex 1}}
       [component]])))

(views/defview main-views []
  [react/view {:style styles/main-views}
   [react/view {:style styles/left-sidebar}
    [react/view {:style {:flex 1}}
     [tab-views]]
    [tabs/main-tabs]]
   [react/view {:style styles/pane-separator}]
   [main-view]])
