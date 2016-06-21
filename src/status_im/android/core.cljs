(ns status-im.android.core
  (:require-macros
    [natal-shell.back-android :refer [add-event-listener remove-event-listener]])
  (:require [reagent.core :as r :refer [atom]]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [status-im.handlers]
            [status-im.subs]
            [status-im.components.react :refer [navigator app-registry device-event-emitter
                                                orientation]]
            [status-im.components.main-tabs :refer [main-tabs]]
            [status-im.contacts.views.contact-list :refer [contact-list] ]
            [status-im.contacts.views.new-contact :refer [new-contact]]
            [status-im.qr-scanner.screen :refer [qr-scanner]]
            [status-im.discovery.screen :refer [discovery]]
            [status-im.discovery.tag :refer [discovery-tag]]
            [status-im.chat.screen :refer [chat]]
            [status-im.chats-list.screen :refer [chats-list]]
            [status-im.new-group.screen :refer [new-group]]
            [status-im.participants.views.add :refer [new-participants]]
            [status-im.participants.views.remove :refer [remove-participants]]
            [status-im.group-settings.screen :refer [group-settings]]
            [status-im.profile.screen :refer [profile my-profile]]
            [status-im.utils.utils :refer [toast]]
            [status-im.utils.encryption]))

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

(defn app-root []
  (let [signed-up (subscribe [:get :signed-up])
        view-id   (subscribe [:get :view-id])]
    (r/create-class
      {:component-will-mount
       (fn []
         (let [o (orientation->keyword (.getInitialOrientation orientation))]
           (dispatch [:set :orientation o]))
         (.addOrientationListener
           orientation
           #(dispatch [:set :orientation (orientation->keyword %)]))
         (.addListener device-event-emitter
                       "keyboardDidShow"
                       (fn [e]
                         (let [h (.. e -endCoordinates -height)]
                           (dispatch [:set :keyboard-height h]))))
         (.addListener device-event-emitter
                       "keyboardDidHide"
                       #(dispatch [:set :keyboard-height 0])))
       :render
       (fn []
         (case (if @signed-up @view-id :chat)
           :discovery [main-tabs]
           :discovery-tag [discovery-tag]
           :add-participants [new-participants]
           :remove-participants [remove-participants]
           :chat-list [main-tabs]
           :new-group [new-group]
           :group-settings [group-settings]
           :contact-list [main-tabs]
           :group-contacts [contact-list]
           :new-contact [new-contact]
           :qr-scanner [qr-scanner]
           :chat [chat]
           :profile [profile]
           :my-profile [my-profile]))})))

(defn init []
  (dispatch-sync [:initialize-db])
  (dispatch [:initialize-crypt])
  (dispatch [:initialize-chats])
  (dispatch [:initialize-protocol])
  (dispatch [:load-user-phone-number])
  (dispatch [:load-contacts])
  ;; load commands from remote server (todo: uncomment)
  ;; (dispatch [:load-commands])
  (dispatch [:init-console-chat])
  (dispatch [:init-chat])
  (init-back-button-handler!)
  (.registerComponent app-registry "StatusIm" #(r/reactify-component app-root)))
