(ns status-im.ui.screens.chat.actions
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.list-selection :as list-selection]
            [status-im.utils.universal-links.core :as universal-links]))

(def view-my-wallet
  {:label  (i18n/label :t/view-my-wallet)
   :action #(re-frame/dispatch [:navigate-to :wallet-modal])})

(defn view-profile [chat-id]
  {:label  (i18n/label :t/view-profile)
   :action #(re-frame/dispatch [:chat.ui/show-profile chat-id])})

(defn group-info [chat-id]
  {:label  (i18n/label :t/group-info)
   :action #(re-frame/dispatch [:show-group-chat-profile chat-id])})

(defn- share-chat [chat-id]
  (let [link    (universal-links/generate-link :public-chat :external chat-id)
        message (i18n/label :t/share-public-chat-text {:link link})]
    {:label  (i18n/label :t/share-chat)
     :action #(list-selection/open-share {:message message})}))

(defn- clear-history []
  {:label  (i18n/label :t/clear-history)
   :action #(re-frame/dispatch [:chat.ui/clear-history-pressed])})

(defn fetch-history [chat-id]
  {:label  (i18n/label :t/fetch-history)
   :action #(re-frame/dispatch [:chat.ui/fetch-history-pressed chat-id])})

(defn- delete-chat [chat-id group?]
  {:label  (i18n/label :t/delete-chat)
   :action #(re-frame/dispatch [(if group?
                                  :group-chats.ui/remove-chat-pressed
                                  :chat.ui/remove-chat-pressed)
                                chat-id])})

(defn- chat-actions [chat-id]
  [view-my-wallet
   (view-profile chat-id)
   (clear-history)
   (fetch-history chat-id)
   (delete-chat chat-id false)])

(defn- group-chat-actions [chat-id]
  [view-my-wallet
   (group-info chat-id)
   (clear-history)
   (fetch-history chat-id)
   (delete-chat chat-id true)])

(defn- public-chat-actions [chat-id]
  [view-my-wallet
   (share-chat chat-id)
   (clear-history)
   (fetch-history chat-id)
   (delete-chat chat-id false)])

(defn actions [group-chat? chat-id public?]
  (cond
    public?     (public-chat-actions chat-id)
    group-chat? (group-chat-actions chat-id)
    :else       (chat-actions chat-id)))
