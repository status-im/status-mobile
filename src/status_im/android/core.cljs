(ns status-im.android.core
  (:require-macros
    [natal-shell.back-android :refer [add-event-listener remove-event-listener]])
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.handlers]
            [status-im.subs]
            [status-im.components.react :refer [app-registry
                                                keyboard
                                                orientation
                                                view
                                                modal
                                                splash-screen]]
            [status-im.components.main-tabs :refer [main-tabs]]
            [status-im.contacts.views.contact-list :refer [contact-list]]
            [status-im.contacts.views.new-contact :refer [new-contact]]
            [status-im.qr-scanner.screen :refer [qr-scanner]]
            [status-im.discover.search-results :refer [discover-search-results]]
            [status-im.chat.screen :refer [chat]]
            [status-im.accounts.login.screen :refer [login]]
            [status-im.accounts.recover.screen :refer [recover]]
            [status-im.accounts.screen :refer [accounts]]
            [status-im.transactions.screen :refer [confirm]]
            [status-im.chats-list.screen :refer [chats-list]]
            [status-im.new-group.screen :refer [new-group]]
            [status-im.participants.views.add :refer [new-participants]]
            [status-im.participants.views.remove :refer [remove-participants]]
            [status-im.group-settings.screen :refer [group-settings]]
            [status-im.profile.screen :refer [profile my-profile]]
            [status-im.profile.photo-capture.screen :refer [profile-photo-capture]]
            status-im.data-store.core
            [taoensso.timbre :as log]
            [status-im.components.status :as status]
            [status-im.chat.styles.screen :as st]
            [status-im.accounts.views.qr-code :refer [qr-code-view]]))

(defn init-back-button-handler! []
  (let [new-listener (fn []
                       ;; todo: it might be better always return false from
                       ;; this listener and handle application's closing
                       ;; in handlers
                       (let [stack (subscribe [:get :navigation-stack])]
                         (when (< 1 (count @stack))
                           (dispatch [:navigate-back])
                           true)))]
    (add-event-listener "hardwareBackPress" new-listener)))

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
                             (dispatch [:set :keyboard-height h])))))
         (.addListener keyboard
                       "keyboardDidHide"
                       #(when-not (= 0 @keyboard-height)
                          (dispatch [:set :keyboard-height 0])))
         (.hide splash-screen))
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
                               :new-group new-group
                               :group-settings group-settings
                               :contact-list main-tabs
                               :group-contacts contact-list
                               :new-contact new-contact
                               :qr-scanner qr-scanner
                               :chat chat
                               :profile profile
                               :profile-photo-capture profile-photo-capture
                               :accounts accounts
                               :login login
                               :recover recover
                               :confirm confirm
                               :my-profile my-profile)]
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
                                      :confirm confirm
                                      :contact-list-modal contact-list)]
                      [component])]])]))))})))

(defn init [& [env]]
  (dispatch-sync [:reset-app])
  (.registerComponent app-registry "StatusIm" #(r/reactify-component app-root))
  (dispatch [:listen-to-network-status!])
  (dispatch [:initialize-crypt])
  (dispatch [:initialize-geth])
  (status/set-soft-input-mode status/adjust-resize)
  (dispatch [:load-user-phone-number])
  (init-back-button-handler!))
