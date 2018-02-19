(ns status-im.chat.views.actions
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.utils :as utils]))

(defn view-profile [chat-id group?]
  {:label  (i18n/label :t/view-profile)
   :action #(re-frame/dispatch [ (if group? :show-group-chat-profile :show-profile) chat-id])})

(defn delete-confirmation [title message dispatch-fn]
  (utils/show-confirmation
    (str (i18n/label title) "?") (i18n/label message) (i18n/label :t/delete)
    (fn[]
      (dispatch-fn))))

(defn delete-chat [chat-id]
  (let [dispatch-fn
        ;; TODO(jeluard) Refactor this or Jan will have an heart attack
        #(do (re-frame/dispatch [:remove-chat chat-id])
             (re-frame/dispatch [:navigation-replace :home]))]
    {:label   (i18n/label :t/delete-chat)
     :action  #(delete-confirmation :t/delete-chat :t/delete-chat-confirmation dispatch-fn)}))

(defn leave-group-chat [chat-id]
  (let [dispatch-fn #(re-frame/dispatch [:leave-group-chat chat-id])]
    {:label   (i18n/label :t/leave-group-chat)
     :action  #(delete-confirmation :t/delete-group :t/delete-group-confirmation dispatch-fn)}))

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
