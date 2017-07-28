(ns status-im.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.handlers]
            [status-im.subs]
            [status-im.specs]
            [status-im.components.react :refer [view
                                                modal
                                                app-registry
                                                keyboard
                                                orientation
                                                splash-screen
                                                http-bridge]]
            [status-im.components.main-tabs :refer [main-tabs]]
            [status-im.contacts.contact-list.views :refer [contact-list]]
            [status-im.contacts.contact-list-modal.views :refer [contact-list-modal]]
            [status-im.contacts.new-contact.views :refer [new-contact]]
            [status-im.qr-scanner.screen :refer [qr-scanner]]
            [status-im.discover.search-results :refer [discover-search-results]]
            [status-im.chat.screen :refer [chat]]
            [status-im.accounts.login.screen :refer [login]]
            [status-im.accounts.recover.screen :refer [recover]]
            [status-im.accounts.screen :refer [accounts]]
            [status-im.transactions.screens.confirmation-success :refer [confirmation-success]]
            [status-im.transactions.screens.unsigned-transactions :refer [unsigned-transactions]]
            [status-im.transactions.screens.transaction-details :refer [transaction-details]]
            [status-im.chats-list.screen :refer [chats-list]]
            [status-im.new-chat.screen :refer [new-chat]]
            [status-im.new-group.screen-private :refer [new-group
                                                        edit-group]]
            [status-im.new-group.views.chat-group-settings :refer [chat-group-settings]]
            [status-im.new-group.views.contact-list :refer [edit-group-contact-list
                                                            edit-chat-group-contact-list]]
            [status-im.new-group.views.contact-toggle-list :refer [contact-toggle-list
                                                                   add-contacts-toggle-list
                                                                   add-participants-toggle-list]]
            [status-im.new-group.views.reorder-groups :refer [reorder-groups]]
            [status-im.new-group.screen-public :refer [new-public-group]]
            [status-im.profile.screen :refer [profile my-profile]]
            [status-im.profile.edit.screen :refer [edit-my-profile]]
            [status-im.profile.photo-capture.screen :refer [profile-photo-capture]]
            status-im.data-store.core
            [taoensso.timbre :as log]
            [status-im.chat.styles.screen :as st]
            [status-im.profile.qr-code.screen :refer [qr-code-view]]
            [status-im.components.status :as status]
            [status-im.utils.utils :as utils]))

(defn orientation->keyword [o]
  (keyword (.toLowerCase o)))

(defn validate-current-view
  [current-view signed-up?]
  (if (or (contains? #{:login :chat :recover :accounts} current-view)
          signed-up?)
    current-view
    :chat))

(defn app-root []
  (let [signed-up?      (subscribe [:signed-up?])
        modal-view      (subscribe [:get :modal])
        view-id         (subscribe [:get :view-id])
        account-id      (subscribe [:get :current-account-id])
        keyboard-height (subscribe [:get :keyboard-height])]
    (log/debug "Current account: " @account-id)
    (r/create-class
      {:component-will-mount
       (fn []
         (let [o (orientation->keyword (.getInitialOrientation orientation))]
           (dispatch [:set :orientation o]))
         (.addOrientationListener
           orientation
           #(dispatch [:set :orientation (orientation->keyword %)]))
         (.lockToPortrait orientation)
         (.addListener keyboard
                       "keyboardWillShow"
                       (fn [e]
                         (let [h (.. e -endCoordinates -height)]
                           (when-not (= h @keyboard-height)
                             (dispatch [:set :keyboard-height h])
                             (dispatch [:set :keyboard-max-height h])))))
         (.addListener keyboard
                       "keyboardWillHide"
                       #(when-not (= 0 @keyboard-height)
                          (dispatch [:set :keyboard-height 0])))
         (.hide splash-screen))
       :component-will-unmount
       (fn []
         (.stop http-bridge))
       :render
       (fn []
         (when @view-id
           (let [current-view (validate-current-view @view-id @signed-up?)]
             (let [component (case current-view
                               :discover main-tabs
                               :discover-search-results discover-search-results
                               :chat-list main-tabs
                               :new-chat new-chat
                               :new-group new-group
                               :edit-group edit-group
                               :chat-group-settings chat-group-settings
                               :edit-group-contact-list edit-group-contact-list
                               :edit-chat-group-contact-list edit-chat-group-contact-list
                               :add-contacts-toggle-list add-contacts-toggle-list
                               :add-participants-toggle-list add-participants-toggle-list
                               :reorder-groups reorder-groups
                               :new-public-group new-public-group
                               :contact-list main-tabs
                               :contact-toggle-list contact-toggle-list
                               :group-contacts contact-list
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

               [view
                {:flex 1}
                [component]
                (when @modal-view
                  [view
                   st/chat-modal
                   [modal {:animation-type   :slide
                           :transparent      false
                           :on-request-close #(dispatch [:navigate-back])}
                    (let [component (case @modal-view
                                      :qr-scanner qr-scanner
                                      :qr-code-view qr-code-view
                                      :unsigned-transactions unsigned-transactions
                                      :transaction-details transaction-details
                                      :confirmation-success confirmation-success
                                      :contact-list-modal contact-list-modal)]
                      [component])]])]))))})))

(defn init []
  (utils/register-exception-handler)
  (status/call-module status/init-jail)
  (dispatch-sync [:reset-app])
  (dispatch [:listen-to-network-status!])
  (dispatch [:initialize-crypt])
  (dispatch [:initialize-geth])
  (.registerComponent app-registry "StatusIm" #(r/reactify-component app-root)))
