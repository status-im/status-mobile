(ns status-im.chat.views.actions
  (:require [re-frame.core :as re-frame]
            [status-im.thread :as status-im.thread]
            [status-im.i18n :as i18n]))

(defn view-profile [chat-id]
  {:label  (i18n/label :t/view-profile)
   :action #(status-im.thread/dispatch [:show-profile chat-id])})

(defn group-info [chat-id]
  {:label  (i18n/label :t/group-info)
   :action #(status-im.thread/dispatch [:show-group-chat-profile chat-id])})

(defn- clear-history []
  {:label  (i18n/label :t/clear-history)
   :action #(status-im.thread/dispatch [:clear-history?])})

(defn- delete-chat [chat-id group?]
  {:label  (i18n/label :t/delete-chat)
   :action #(status-im.thread/dispatch [:remove-chat-and-navigate-home? chat-id group?])})

(defn- chat-actions [chat-id]
  [(view-profile chat-id)
   (clear-history)
   (delete-chat chat-id false)])

(defn- group-chat-actions [chat-id]
  [(group-info chat-id)
   (clear-history)
   (delete-chat chat-id true)])

(defn- public-chat-actions [chat-id]
  [(clear-history)
   (delete-chat chat-id true)])

(defn actions [group-chat? chat-id public?]
  (cond
    public?     (public-chat-actions chat-id)
    group-chat? (group-chat-actions chat-id)
    :else       (chat-actions chat-id)))
