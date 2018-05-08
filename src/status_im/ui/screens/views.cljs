(ns status-im.ui.screens.views
  (:require-macros [status-im.utils.views :refer [defview letsubs] :as views])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.utils.platform :refer [android?]]
            [status-im.ui.components.react :refer [view modal create-main-screen-view] :as react]
            [status-im.ui.components.styles :as common-styles]
            [status-im.ui.screens.main-tabs.views :refer [main-tabs]]

            [status-im.ui.screens.accounts.login.views :refer [login]]
            [status-im.ui.screens.accounts.recover.views :refer [recover]]
            [status-im.ui.screens.accounts.views :refer [accounts]]

            [status-im.chat.screen :refer [chat]]
            [status-im.ui.screens.add-new.views :refer [add-new]]
            [status-im.ui.screens.add-new.new-chat.views :refer [new-chat]]
            [status-im.ui.screens.add-new.new-public-chat.view :refer [new-public-chat]]

            [status-im.ui.screens.contacts.contact-list-modal.views :refer [contact-list-modal]]

            [status-im.ui.screens.qr-scanner.views :refer [qr-scanner]]

            [status-im.ui.screens.group.views :refer [new-group]]
            [status-im.ui.screens.group.edit-contacts.views :refer [edit-contact-group-contact-list]]
            [status-im.ui.screens.group.add-contacts.views :refer [contact-toggle-list
                                                                   add-contacts-toggle-list
                                                                   add-participants-toggle-list]]

            [status-im.ui.screens.profile.user.views :as profile.user]
            [status-im.ui.screens.profile.contact.views :as profile.contact]
            [status-im.ui.screens.profile.group-chat.views :as profile.group-chat]
            [status-im.ui.screens.profile.photo-capture.views :refer [profile-photo-capture]]
            [status-im.ui.screens.wallet.send.views :refer [send-transaction send-transaction-modal sign-message-modal]]
            [status-im.ui.screens.wallet.choose-recipient.views :refer [choose-recipient]]
            [status-im.ui.screens.wallet.request.views :refer [request-transaction send-transaction-request]]
            [status-im.ui.screens.wallet.components.views :as wallet.components]
            [status-im.ui.screens.wallet.send.views :as wallet.send]
            [status-im.ui.screens.wallet.settings.views :as wallet-settings]
            [status-im.ui.screens.wallet.transactions.views :as wallet-transactions]
            [status-im.ui.screens.wallet.send.transaction-sent.views :refer [transaction-sent transaction-sent-modal]]
            [status-im.ui.screens.wallet.components.views :refer [contact-code recent-recipients recipient-qr-code]]
            [status-im.ui.screens.network-settings.views :refer [network-settings]]
            [status-im.ui.screens.network-settings.network-details.views :refer [network-details]]
            [status-im.ui.screens.offline-messaging-settings.views :refer [offline-messaging-settings]]
            [status-im.ui.screens.browser.views :refer [browser]]
            [status-im.ui.screens.add-new.open-dapp.views :refer [open-dapp dapp-description]]
            [status-im.ui.screens.intro.views :refer [intro]]
            [status-im.ui.screens.accounts.create.views :refer [create-account]]
            [status-im.ui.screens.usage-data.views :refer [usage-data]]
            [status-im.ui.screens.profile.seed.views :refer [backup-seed]]
            [status-im.utils.config :as config]))

;;; defines hierarchy of views, when parent screen is opened children screens
;;; are pre-rendered, currently it is:
;;;
;;; root-
;;;      |
;;;      - main-tabs -
;;;      |           |
;;;      - chat      |
;;;                  wallet
;;;                  - wallet-send-transaction -
;;;                  |                         |
;;;                  |                         - choose-recipient
;;;                  |                         |
;;;                  |                         - wallet-transaction-sent
;;;                  |
;;;                  - transactions-history, unsigned-transactions
;;;                  |
;;;                  - wallet-request-transaction -
;;;                  |                            |
;;;                  |                            - choose-recipient
;;;                  |
;;;                  my-profile
;;;                  - edit-my-profile -
;;;                                    |
;;;                                    - profile-photo-capture
(views/compile-views root-view
                     [{:views     #{:home :wallet :my-profile}
                       :component main-tabs}

                      {:view      :chat
                       :hide?     (not android?)
                       :component chat}

                      {:view      :wallet-send-transaction
                       :parent    :wallet
                       :hide?     (not android?)
                       :component send-transaction}

                      {:view      :wallet-request-transaction
                       :parent    :wallet
                       :component request-transaction}

                      {:view      :wallet-request-assets
                       :parent    :wallet-request-transaction
                       :component wallet.components/request-assets}

                      {:view      :choose-recipient
                       :parent    :wallet-send-transaction
                       :hide?     true
                       :component choose-recipient}

                      {:view      :wallet-transaction-sent
                       :parent    :wallet-send-transaction
                       :component transaction-sent}

                      {:views     #{:transactions-history :unsigned-transactions}
                       :parent    :wallet
                       :component wallet-transactions/transactions}

                      {:view      :profile-photo-capture
                       :parent    :my-profile
                       :component profile-photo-capture}])

(defview main []
  (letsubs [signed-up? [:signed-up?]
            view-id    [:get :view-id]
            modal-view [:get :modal]]
    {:component-will-update (fn [] (react/dismiss-keyboard!))}
    (when view-id
      (let [component (case view-id
                        :intro intro
                        :create-account create-account
                        :usage-data usage-data
                        (:home :wallet :my-profile) main-tabs
                        :browser browser
                        :open-dapp open-dapp
                        :dapp-description dapp-description
                        :wallet-send-transaction send-transaction
                        :wallet-transaction-sent transaction-sent
                        :wallet-request-transaction request-transaction
                        :wallet-send-transaction-request send-transaction-request
                        (:transactions-history :unsigned-transactions) wallet-transactions/transactions
                        :wallet-transaction-details wallet-transactions/transaction-details
                        :wallet-send-assets wallet.components/send-assets
                        :wallet-request-assets wallet.components/request-assets
                        :new add-new
                        :new-group new-group
                        :add-contacts-toggle-list add-contacts-toggle-list
                        :add-participants-toggle-list add-participants-toggle-list
                        :edit-group-contact-list edit-contact-group-contact-list
                        :new-public-chat new-public-chat
                        :contact-toggle-list contact-toggle-list
                        :new-chat new-chat
                        :qr-scanner qr-scanner
                        :chat chat
                        :profile profile.contact/profile
                        :group-chat-profile profile.group-chat/group-chat-profile
                        :profile-photo-capture profile-photo-capture
                        :accounts accounts
                        :login login
                        :recover recover
                        :network-settings network-settings
                        :network-details network-details
                        :offline-messaging-settings offline-messaging-settings
                        :recent-recipients recent-recipients
                        :recipient-qr-code recipient-qr-code
                        :contact-code contact-code
                        :profile-qr-viewer profile.user/qr-viewer
                        :backup-seed backup-seed
                        [react/view [react/text (str "Unknown view: " view-id)]])
            main-screen-view (create-main-screen-view view-id)]
        [main-screen-view common-styles/flex
         (if (and config/compile-views-enabled?
                  signed-up?
                  (#{:home :wallet :my-profile :chat :wallet-send-transaction
                     :choose-recipient :wallet-transaction-sent :transactions-history
                     :unsigned-transactions :wallet-request-transaction :edit-my-profile
                     :profile-photo-capture :wallet-request-assets}
                   view-id))
           [root-view]
           [component])
         (when modal-view
           [view common-styles/modal
            [modal {:animation-type   :slide
                    :transparent      true
                    :on-request-close #(dispatch [:navigate-back])}
             (let [component (case modal-view
                               :qr-scanner qr-scanner
                               :contact-list-modal contact-list-modal
                               :wallet-transactions-filter wallet-transactions/filter-history
                               :wallet-settings-assets wallet-settings/manage-assets
                               :wallet-send-transaction-modal send-transaction-modal
                               :wallet-transaction-sent-modal transaction-sent-modal
                               :wallet-sign-message-modal sign-message-modal
                               :wallet-transaction-fee wallet.send/transaction-fee
                               [react/view [react/text (str "Unknown modal view: " modal-view)]])]
               [react/main-screen-modal-view modal-view
                [component]])]])]))))
