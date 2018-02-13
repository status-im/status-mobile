(ns status-im.chat.views.actions
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]))

(defn view-profile [chat-id group?]
  {:label  (i18n/label :t/view-profile)
   :action #(re-frame/dispatch [ (if group? :show-group-chat-profile :show-profile) chat-id])})

(defn delete-chat [chat-id]
  {:label   (i18n/label :t/delete-chat)
   ;; TODO(jeluard) Refactor this or Jan will have an heart attack
   :action  #(do (re-frame/dispatch [:remove-chat chat-id])
                 (re-frame/dispatch [:navigation-replace :home]))})

(defn leave-group-chat [chat-id]
  {:label   (i18n/label :t/leave-group-chat)
   :action  #(re-frame/dispatch [:leave-group-chat chat-id])})

(defn- user-chat-actions [chat-id group?]
  [(view-profile chat-id group?)
   (delete-chat chat-id)])

(defn- group-chat-actions [chat-id]
  (into (user-chat-actions chat-id true)
        [(leave-group-chat chat-id)]))

(defn actions [group-chat? chat-id]
  (if group-chat?
    (group-chat-actions chat-id)
    (user-chat-actions chat-id false)))
