(ns status-im.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [re-frame.core :refer [dispatch]]
            [status-im.utils.platform :refer [android?]]
            [status-im.components.react :refer [view modal]]
            [status-im.components.styles :as common-styles]
            [status-im.components.main-tabs :refer [main-tabs]]
            [status-im.components.context-menu :refer [menu-context]]

            [status-im.accounts.login.screen :refer [login]]
            [status-im.accounts.recover.screen :refer [recover]]
            [status-im.accounts.screen :refer [accounts]]

            [status-im.chat.screen :refer [chat]]
            [status-im.chat.new-chat.view :refer [new-chat]]
            [status-im.chat.new-public-chat.view :refer [new-public-chat]]
            [status-im.chats-list.screen :refer [chats-list]]

            [status-im.contacts.contact-list.views :refer [contact-list]]
            [status-im.contacts.contact-list-modal.views :refer [contact-list-modal]]
            [status-im.contacts.new-contact.views :refer [new-contact]]

            [status-im.discover.search-results :refer [discover-search-results]]

            [status-im.qr-scanner.screen :refer [qr-scanner]]

            [status-im.transactions.screens.confirmation-success :refer [confirmation-success]]
            [status-im.transactions.screens.unsigned-transactions :refer [unsigned-transactions]]
            [status-im.transactions.screens.transaction-details :refer [transaction-details]]


            [status-im.group.views :refer [new-group edit-contact-group]]
            [status-im.group.chat-settings.views :refer [chat-group-settings]]
            [status-im.group.edit-contacts.views :refer [edit-contact-group-contact-list
                                                         edit-chat-group-contact-list]]
            [status-im.group.add-contacts.views :refer [contact-toggle-list
                                                        add-contacts-toggle-list
                                                        add-participants-toggle-list]]
            [status-im.group.reorder.views :refer [reorder-groups]]

            [status-im.profile.screen :refer [profile my-profile]]
            [status-im.profile.edit.screen :refer [edit-my-profile]]
            [status-im.profile.photo-capture.screen :refer [profile-photo-capture]]
            [status-im.profile.qr-code.screen :refer [qr-code-view]]))

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
                          :wallet main-tabs
                          :discover main-tabs
                          :discover-search-results discover-search-results
                          :chat-list main-tabs
                          :new-chat new-chat
                          :new-group new-group
                          :edit-contact-group edit-contact-group
                          :chat-group-settings chat-group-settings
                          :add-contacts-toggle-list add-contacts-toggle-list
                          :add-participants-toggle-list add-participants-toggle-list
                          :edit-group-contact-list edit-contact-group-contact-list
                          :edit-chat-group-contact-list edit-chat-group-contact-list
                          :new-public-chat new-public-chat
                          :contact-list main-tabs
                          :contact-toggle-list contact-toggle-list
                          :group-contacts contact-list
                          :reorder-groups reorder-groups
                          :new-contact new-contact
                          :qr-scanner qr-scanner
                          :chat chat
                          :profile profile
                          :my-profile my-profile
                          :edit-my-profile edit-my-profile
                          :profile-photo-capture profile-photo-capture
                          :accounts accounts
                          :login login
                          :recover recover)]

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
                                  :unsigned-transactions unsigned-transactions
                                  :transaction-details transaction-details
                                  :confirmation-success confirmation-success
                                  :contact-list-modal contact-list-modal)]
                  [component])]])]])))))