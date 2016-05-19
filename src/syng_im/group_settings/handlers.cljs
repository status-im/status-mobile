(ns syng-im.group-settings.handlers
  (:require [re-frame.core :refer [register-handler debug dispatch]]
            [syng-im.persistence.realm :as r]
            [syng-im.models.messages :refer [clear-history]]))

(defn set-chat-name [db]
  (let [chat-id (:current-chat-id db)
        name    (:new-chat-name db)]
    (r/write (fn []
               (-> (r/get-by-field :chats :chat-id chat-id)
                   (r/single)
                   (aset "name" name))))
    (assoc-in db [:chats chat-id :name] name)))

(defn set-chat-color [db]
  (let [chat-id (:current-chat-id db)
        color   (:new-chat-color db)]
    (r/write (fn []
               (-> (r/get-by-field :chats :chat-id chat-id)
                   (r/single)
                   (aset "color" color))))
    (assoc-in db [:chats chat-id :color] color)))

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
    (let [chat-id    (:current-chat-id db)
          chat-name  (get-in db [:chats chat-id :name])
          chat-color (get-in db [:chats chat-id :color])
          db         (assoc db
                            :new-chat-name                    chat-name
                            :new-chat-color                   chat-color
                            :group-settings-show-color-picker false
                            :group-settings-selected-member   nil)]
      (dispatch [:navigate-to :group-settings])
      db)))

(register-handler :set-chat-name
  (fn [db [action]]
    (set-chat-name db)))

(register-handler :set-chat-color
  (fn [db [action]]
    (set-chat-color db)))

(register-handler :set-new-chat-name
  (fn [db [action name]]
    (assoc db :new-chat-name name)))

(register-handler :set-new-chat-color
  (fn [db [action color]]
    (assoc db :new-chat-color color)))

(register-handler :select-group-chat-member
  (fn [db [action identity]]
    (assoc db :group-settings-selected-member identity)))

(register-handler :set-group-settings-show-color-picker
  (fn [db [action show?]]
    (assoc db :group-settings-show-color-picker show?)))

(register-handler :clear-history
  (fn [db [action]]
    (clear-history (:current-chat-id db))))
