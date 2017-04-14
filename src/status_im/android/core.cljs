(ns status-im.android.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.handlers]
            [status-im.subs]
            [status-im.components.react :refer [app-registry
                                                keyboard
                                                orientation
                                                back-android
                                                view
                                                modal
                                                splash-screen
                                                http-bridge]]
            [status-im.components.main-tabs :refer [main-tabs]]
            [status-im.components.context-menu :refer [menu-context]]
            [status-im.contacts.views.contact-list :refer [contact-list]]
            [status-im.contacts.views.contact-list-modal :refer [contact-list-modal]]
            [status-im.contacts.views.new-contact :refer [new-contact]]
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
            [status-im.new-group.screen-public :refer [new-public-group]]
            [status-im.new-group.screen-private :refer [new-group
                                                        edit-group]]
            [status-im.new-group.views.chat-group-settings :refer [chat-group-settings]]
            [status-im.new-group.views.contact-list :refer [edit-group-contact-list
                                                            edit-chat-group-contact-list]]
            [status-im.new-group.views.contact-toggle-list :refer [contact-toggle-list
                                                                   add-contacts-toggle-list
                                                                   add-participants-toggle-list]]
            [status-im.new-group.views.reorder-groups :refer [reorder-groups]]
            [status-im.participants.views.add :refer [new-participants]]
            [status-im.participants.views.remove :refer [remove-participants]]
            [status-im.profile.screen :refer [profile my-profile]]
            [status-im.profile.edit.screen :refer [edit-my-profile]]
            [status-im.profile.photo-capture.screen :refer [profile-photo-capture]]
            status-im.data-store.core
            [taoensso.timbre :as log]
            [status-im.components.status :as status]
            [status-im.components.styles :as st]
            [status-im.chat.styles.screen :as chat-st]
            [status-im.profile.qr-code.screen :refer [qr-code-view]]))

(defn init-back-button-handler! []
  (let [new-listener (fn []
                       ;; todo: it might be better always return false from
                       ;; this listener and handle application's closing
                       ;; in handlers
                       (let [stack (subscribe [:get :navigation-stack])
                             creating? (subscribe [:get :creating-account?])]
                         (cond
                           @creating? true

                           (< 1 (count @stack))
                           (do (dispatch [:navigate-back]) true)

                           :else false)))]
    (.addEventListener back-android "hardwareBackPress" new-listener)))

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
        view-id         (subscribe [:get :view-id])
        account-id      (subscribe [:get :current-account-id])
        keyboard-height (subscribe [:get :keyboard-height])
        modal-view      (subscribe [:get :modal])]
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
                       "keyboardDidShow"
                       (fn [e]
                         (let [h (.. e -endCoordinates -height)]
                           (when-not (= h @keyboard-height)
                             (dispatch [:set :keyboard-height h])
                             (dispatch [:set :keyboard-max-height h])))))
         (.addListener keyboard
                       "keyboardDidHide"
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
                               :add-participants new-participants
                               :remove-participants remove-participants
                               :chat-list main-tabs
                               :new-chat new-chat
                               :new-group new-group
                               :edit-group edit-group
                               :chat-group-settings chat-group-settings
                               :add-contacts-toggle-list add-contacts-toggle-list
                               :add-participants-toggle-list add-participants-toggle-list
                               :edit-group-contact-list edit-group-contact-list
                               :edit-chat-group-contact-list edit-chat-group-contact-list
                               :new-public-group new-public-group
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

               [menu-context st/flex
                [view st/flex
                 [component]
                 (when @modal-view
                   [view chat-st/chat-modal
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
                       [component])]])]]))))})))

(defn init []
  (status/call-module status/init-jail)
  (dispatch-sync [:reset-app])
  (.registerComponent app-registry "StatusIm" #(r/reactify-component app-root))
  (dispatch [:listen-to-network-status!])
  (dispatch [:initialize-crypt])
  (dispatch [:initialize-geth])
  (status/set-soft-input-mode status/adjust-resize)
  (dispatch [:load-user-phone-number])
  (init-back-button-handler!))
