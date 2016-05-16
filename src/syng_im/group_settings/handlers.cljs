(ns syng-im.group-settings.handlers
  (:require [re-frame.core :refer [register-handler debug dispatch]]
            [syng-im.db :as db]
            [syng-im.persistence.realm :as r]))

(defn set-group-chat-name [db name]
  (let [chat-id (:current-chat-id db)]
    (r/write (fn []
               (-> (r/get-by-field :chats :chat-id chat-id)
                   (r/single)
                   (aset "name" name))))
    (assoc-in db (db/chat-name-path chat-id) name)))

(defn set-chat-color [db color]
  (let [chat-id (:current-chat-id db)]
    (r/write (fn []
               (-> (r/get-by-field :chats :chat-id chat-id)
                   (r/single)
                   (aset "color" color))))
    (assoc-in db (db/chat-color-path chat-id) color)))

(defn chat-remove-member [db identity]
  (let [chat (get-in db [:chats (:current-chat-id db)])]
    (r/write
     (fn []
       (r/create :chats
                 (update chat :contacts
                         (fn [members]
                           (filter #(not= (:identity %) identity) members)))
                 true)))
    ;; TODO temp. Update chat in db atom
    (dispatch [:initialize-chats])
    db))

(defn delete-chat [chat-id]
  (r/write
   (fn []
     (-> (r/get-by-field :chats :chat-id chat-id)
         (r/single)
         (r/delete))))
  ;; TODO temp. Update chat in db atom
  (dispatch [:initialize-chats]))

(register-handler :show-group-settings
  (fn [db [action]]
    (dispatch [:navigate-to :group-settings])
    db))

(register-handler :set-group-chat-name
  (fn [db [action chat-name]]
    (set-group-chat-name db chat-name)))

(register-handler :set-chat-color
  (fn [db [action color]]
    (set-chat-color db color)))

(register-handler :select-group-chat-member
  (fn [db [action identity]]
    (assoc-in db db/group-settings-selected-member-path identity)))

(register-handler :show-group-settings-color-picker
  (fn [db [action show?]]
    (assoc-in db db/group-settings-show-color-picker show?)))

(register-handler :chat-remove-member
  (fn [db [action identity]]
    (let [chat-id (:current-chat-id db)
          db      (chat-remove-member db identity)]
      (dispatch [:select-group-chat-member nil])
      ;; TODO fix and uncomment
      ;; (api/group-remove-participant chat-id identity)
      ;; (removed-participant-msg chat-id identity)
      )))

(register-handler :clear-history
  (fn [db [action]]
    (clear-history (:current-chat-id db))))
