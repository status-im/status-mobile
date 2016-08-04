(ns status-im.ios.core
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.handlers]
            [status-im.subs]
            [status-im.ios.styles :refer [styles]]
            [status-im.components.react :refer [app-registry
                                                keyboard
                                                orientation
                                                show-action-sheet]]
            [status-im.components.main-tabs :refer [main-tabs]]
            [status-im.contacts.views.contact-list :refer [contact-list]]
            [status-im.contacts.views.new-contact :refer [new-contact]]
            [status-im.qr-scanner.screen :refer [qr-scanner]]
            [status-im.discovery.tag :refer [discovery-tag]]
            [status-im.chat.screen :refer [chat]]
            [status-im.accounts.login.screen :refer [login]]
            [status-im.accounts.screen :refer [accounts]]
            [status-im.chats-list.screen :refer [chats-list]]
            [status-im.new-group.screen :refer [new-group]]
            [status-im.participants.views.add :refer [new-participants]]
            [status-im.participants.views.remove :refer [remove-participants]]
            [status-im.group-settings.screen :refer [group-settings]]
            [status-im.profile.screen :refer [profile my-profile]]
            [status-im.profile.photo-capture.screen :refer [profile-photo-capture]]
            [status-im.utils.utils :refer [toast]]
            [status-im.utils.encryption]
            status-im.persistence.realm.core
            [status-im.utils.logging :as log]))

(defn orientation->keyword [o]
  (keyword (.toLowerCase o)))

(defn app-root []
  (let [signed-up       (subscribe [:get :signed-up])
        _               (log/debug "signed up: " @signed-up)
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
                       "keyboardDidShow"
                       (fn [e]
                         (let [h (.. e -endCoordinates -height)]
                           (when-not (= h @keyboard-height)
                             (dispatch [:set :keyboard-height h])))))
         (.addListener keyboard
                       "keyboardDidHide"
                       (when-not (= 0 @keyboard-height)
                         #(dispatch [:set :keyboard-height 0]))))
       :render
       (fn []
         (let [startup-view (if @account-id
                              (if @signed-up
                                @view-id
                                :chat)
                              (if (contains? #{:login :chat} @view-id)
                                @view-id
                                :accounts))]
           (log/debug startup-view)
           (let [component (case (if true startup-view :chat)
                             :discovery main-tabs
                             :discovery-tag discovery-tag
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
                             :my-profile my-profile)]
             [component {:platform-specific {:styles            styles
                                             :list-selection-fn show-action-sheet}}])))})))

(defn init []
  (dispatch-sync [:reset-app])
  (dispatch [:initialize-crypt])
  (dispatch [:initialize-geth])
  (dispatch [:load-user-phone-number])
  (.registerComponent app-registry "StatusIm" #(r/reactify-component app-root)))
