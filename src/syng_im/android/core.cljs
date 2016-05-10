(ns syng-im.android.core
  (:require-macros
    [natal-shell.back-android :refer [add-event-listener remove-event-listener]])
  (:require [reagent.core :as r :refer [atom]]
            [cljs.core :as cljs]
            [re-frame.core :refer [subscribe dispatch dispatch-sync]]
            [syng-im.handlers]
            [syng-im.subs]
            [syng-im.components.react :refer [navigator app-registry]]
            [syng-im.components.contact-list.contact-list :refer [contact-list]]
            [syng-im.components.chat :refer [chat]]
            [syng-im.components.chats.chats-list :refer [chats-list]]
            [syng-im.components.chats.new-group :refer [new-group]]
            [syng-im.components.chat.new-participants :refer [new-participants]]
            [syng-im.components.chat.remove-participants :refer [remove-participants]]
            [syng-im.components.profile :refer [profile]]
            [syng-im.utils.logging :as log]
            [syng-im.utils.utils :refer [toast]]
            [syng-im.navigation :as nav]
            [syng-im.utils.encryption]))

(def back-button-handler (cljs/atom {:nav     nil
                                     :handler nil}))

(defn init-back-button-handler! []
  (let [new-listener (fn []
                       ;; todo: it might be better always return false from
                       ;; this listener and handle application's closing
                       ;; in handlers
                       (let [stack (subscribe [:navigation-stack])]
                         (when (< 1 (count stack))
                           (dispatch [:navigate-back])
                           true)))]
    (add-event-listener "hardwareBackPress" new-listener)))

(defn app-root []
  (let [signed-up (subscribe [:signed-up])
        view-id (subscribe [:view-id])]
    (fn []
      (case (if @signed-up @view-id :chat)
        :add-participants [new-participants]
        :remove-participants [remove-participants]
        :chat-list [chats-list]
        :new-group [new-group]
        :contact-list [contact-list]
        :chat [chat]
        :profile [profile]))))

(defn init []
  (dispatch-sync [:initialize-db])
  (dispatch [:initialize-crypt])
  (dispatch [:initialize-chats])
  (dispatch [:initialize-protocol])
  (dispatch [:load-user-phone-number])
  (dispatch [:load-syng-contacts])
  ;; load commands from remote server (todo: uncomment)
  ;; (dispatch [:load-commands])
  (dispatch [:init-console-chat])
  (dispatch [:init-chat])
  (init-back-button-handler!)
  (.registerComponent app-registry "SyngIm" #(r/reactify-component app-root)))
