(ns status-im.ui.screens.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.utils.platform :refer [android?]]
            [status-im.ui.components.react :refer [view modal]]
            [status-im.ui.components.styles :as common-styles]
            [status-im.ui.screens.main-tabs.views :refer [main-tabs]]
            [status-im.ui.components.context-menu :refer [menu-context]]

            [status-im.ui.screens.accounts.login.views :refer [login]]
            [status-im.ui.screens.accounts.recover.views :refer [recover recover-modal]]
            [status-im.ui.screens.accounts.views :refer [accounts]]

            [status-im.chat.screen :refer [chat]]
            [status-im.chat.new-chat.view :refer [new-chat]]
            [status-im.chat.new-public-chat.view :refer [new-public-chat]]

            [status-im.ui.screens.contacts.contact-list.views :refer [contact-list]]
            [status-im.ui.screens.contacts.contact-list-modal.views :refer [contact-list-modal]]
            [status-im.ui.screens.contacts.new-contact.views :refer [new-contact]]

            [status-im.ui.screens.qr-scanner.views :refer [qr-scanner]]

            [status-im.ui.screens.group.views :refer [new-group edit-contact-group]]
            [status-im.ui.screens.group.chat-settings.views :refer [chat-group-settings]]
            [status-im.ui.screens.group.edit-contacts.views :refer [edit-contact-group-contact-list
                                                                    edit-chat-group-contact-list]]
            [status-im.ui.screens.group.add-contacts.views :refer [contact-toggle-list
                                                                   add-contacts-toggle-list
                                                                   add-participants-toggle-list]]
            [status-im.ui.screens.group.reorder.views :refer [reorder-groups]]

            [status-im.ui.screens.profile.views :refer [profile my-profile]]
            [status-im.ui.screens.profile.edit.views :refer [edit-my-profile]]
            [status-im.ui.screens.profile.photo-capture.views :refer [profile-photo-capture]]
            [status-im.ui.screens.profile.qr-code.views :refer [qr-code-view]]

            [status-im.ui.screens.wallet.send.views :refer [send-transaction send-transaction-modal]]
            [status-im.ui.screens.wallet.choose-recipient.views :refer [choose-recipient]]
            [status-im.ui.screens.wallet.request.views :refer [request-transaction]]
            [status-im.ui.screens.wallet.wallet-list.views :refer [wallet-list-screen]]
            [status-im.ui.screens.wallet.transactions.views :as wallet-transactions]
            [status-im.ui.screens.wallet.send.transaction-sent.views :refer [transaction-sent transaction-sent-modal]]
            [status-im.ui.screens.wallet.assets.views :as wallet-assets]

            [status-im.ui.components.status-bar :as status-bar]

            [status-im.ui.screens.discover.search-results.views :as discover-search]
            [status-im.ui.screens.discover.recent-statuses.views :as discover-recent]
            [status-im.ui.screens.discover.all-dapps.views :as discover-all-dapps]
            [status-im.ui.screens.discover.popular-hashtags.views :as discover-popular]
            [status-im.ui.screens.discover.dapp-details.views :as discover-dapp-details]

            [status-im.ui.screens.network-settings.views :refer [network-settings]]
            [status-im.ui.screens.network-settings.add-rpc.views :refer [add-rpc-url]]
            [status-im.ui.screens.network-settings.network-details.views :refer [network-details]]
            [status-im.ui.screens.network-settings.parse-json.views :refer [paste-json-text]]))

(defn validate-current-view
  [current-view signed-up?]
  (if (or (contains? #{:login :chat :recover :accounts} current-view)
          signed-up?)
    current-view
    :chat))

(defview main []
  (letsubs [signed-up? [:signed-up?]
            view-id    [:get :view-id]
            modal-view [:get :modal]]
    (when view-id
      (let [current-view (validate-current-view view-id signed-up?)]
        (let [component (case current-view
                          (:wallet :chat-list :discover :contact-list) main-tabs
                          :wallet-list wallet-list-screen
                          :wallet-send-transaction send-transaction
                          :wallet-transaction-sent transaction-sent
                          :choose-recipient choose-recipient
                          :wallet-request-transaction request-transaction
                          (:transactions-history :unsigned-transactions) wallet-transactions/transactions
                          :wallet-transaction-details wallet-transactions/transaction-details
                          (:wallet-my-token :wallet-market-value) wallet-assets/my-token-main
                          :new-chat new-chat
                          :new-group new-group
                          :edit-contact-group edit-contact-group
                          :chat-group-settings chat-group-settings
                          :add-contacts-toggle-list add-contacts-toggle-list
                          :add-participants-toggle-list add-participants-toggle-list
                          :edit-group-contact-list edit-contact-group-contact-list
                          :edit-chat-group-contact-list edit-chat-group-contact-list
                          :new-public-chat new-public-chat
                          :contact-toggle-list contact-toggle-list
                          :group-contacts contact-list
                          :reorder-groups reorder-groups
                          :new-contact new-contact
                          :qr-scanner qr-scanner
                          :chat chat
                          :profile profile
                          :my-profile my-profile
                          :edit-my-profile edit-my-profile
                          :discover-all-recent discover-recent/discover-all-recent
                          :discover-all-hashtags discover-popular/discover-all-hashtags
                          :discover-search-results discover-search/discover-search-results
                          :discover-dapp-details discover-dapp-details/dapp-details
                          :discover-all-dapps discover-all-dapps/main
                          :profile-photo-capture profile-photo-capture
                          :accounts accounts
                          :login login
                          :recover recover
                          :network-settings network-settings
                          :paste-json-text paste-json-text
                          :add-rpc-url add-rpc-url
                          :network-details network-details
                          (throw (str "Unknown view: " current-view)))]
          [(if android? menu-context view) common-styles/flex
           [view common-styles/flex
            [component]
            (when modal-view
              [view common-styles/modal
               [modal {:animation-type   :slide
                       :transparent      false
                       :on-request-close #(dispatch [:navigate-back])}
                (let [component (case modal-view
                                  :qr-scanner qr-scanner
                                  :qr-code-view qr-code-view
                                  :recover-modal recover-modal
                                  :contact-list-modal contact-list-modal
                                  :wallet-transactions-filter wallet-transactions/filter-history
                                  :wallet-send-transaction-modal send-transaction-modal
                                  :wallet-transaction-sent-modal transaction-sent-modal
                                  (throw (str "Unknown modal view: " modal-view)))]
                  [component])]])]])))))
