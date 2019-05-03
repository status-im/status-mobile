(ns status-im.ui.screens.chat.actions
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.utils.platform :as platform]))

(defn view-profile [chat-id]
  {:label  (i18n/label :t/view-profile)
   :action #(re-frame/dispatch [:chat.ui/show-profile chat-id])})

(defn group-info [chat-id]
  {:label  (i18n/label :t/group-info)
   :action #(re-frame/dispatch [:show-group-chat-profile chat-id])})

(defn- share-chat [chat-id]
  (let [link    (universal-links/generate-link :public-chat :external chat-id)
        message (i18n/label :t/share-public-chat-text {:link link})]
    (when-not platform/desktop?
      {:label  (i18n/label :t/share-chat)
       :action #(list-selection/open-share {:message message})})))

(defn- clear-history []
  {:label  (i18n/label :t/clear-history)
   :action #(re-frame/dispatch [:chat.ui/clear-history-pressed])})

(defn fetch-history [chat-id]
  {:label  (i18n/label :t/fetch-history)
   :action #(re-frame/dispatch [:chat.ui/fetch-history-pressed chat-id])})

(defn fetch-history48-60 [chat-id]
  {:label  "Fetch 48-60h"
   :action #(re-frame/dispatch [:chat.ui/fetch-history-pressed48-60 chat-id])})

(defn fetch-history84-96 [chat-id]
  {:label  "Fetch 84-96h"
   :action #(re-frame/dispatch [:chat.ui/fetch-history-pressed84-96 chat-id])})

(defn- delete-chat [chat-id group?]
  {:label  (i18n/label :t/delete-chat)
   :action #(re-frame/dispatch [(if group?
                                  :group-chats.ui/remove-chat-pressed
                                  :chat.ui/remove-chat-pressed)
                                chat-id])})

(defn- chat-actions [chat-id]
  [(view-profile chat-id)
   (clear-history)
   (fetch-history chat-id)
   #_(fetch-history48-60 chat-id)
   #_(fetch-history84-96 chat-id)
   (delete-chat chat-id false)])

(defn- group-chat-actions [chat-id]
  [(group-info chat-id)
   (clear-history)
   (fetch-history chat-id)
   #_(fetch-history48-60 chat-id)
   #_(fetch-history84-96 chat-id)
   (delete-chat chat-id true)])

(defn- public-chat-actions [chat-id]
  [(share-chat chat-id)
   (clear-history)
   (fetch-history chat-id)
   #_(fetch-history48-60 chat-id)
   #_(fetch-history84-96 chat-id)
   (delete-chat chat-id false)])

(defn actions [group-chat? chat-id public?]
  (cond
    public?     (public-chat-actions chat-id)
    group-chat? (group-chat-actions chat-id)
    :else       (chat-actions chat-id)))
