(ns syng-im.group-settings.handlers
  (:require [re-frame.core :refer [register-handler debug dispatch after]]
            [syng-im.persistence.realm :as r]
            [syng-im.models.messages :refer [clear-history]]
            [clojure.string :as s]))

(defn save-chat-property!
  [db-name property-name]
  (fn [{:keys [current-chat-id] :as db} _]
    (let [property (db-name db)]
      (r/write (fn []
                 (-> (r/get-by-field :chats :chat-id current-chat-id)
                     (r/single)
                     (aset (name property-name) property)))))))

(defn update-chat-property
  [db-name property-name]
  (fn [{:keys [current-chat-id] :as db} _]
    (let [property (db-name db)]
      (assoc-in db [:chats current-chat-id property-name] property))))

(defn delete-chat [chat-id]
  (r/write
    (fn []
      (-> (r/get-by-field :chats :chat-id chat-id)
          (r/single)
          (r/delete))))
  ;; TODO temp. Update chat in db atom
  (dispatch [:initialize-chats]))

(defn prepare-chat-settings
  [{:keys [current-chat-id] :as db} _]
  (let [{:keys [name color]} (-> db
                                 (get-in [:chats current-chat-id])
                                 (select-keys [:name :color]))]
    (-> db
        (assoc :new-chat-name name
               :new-chat-color color
               :group-settings {}))))

(register-handler :show-group-settings
  (after (fn [_ _] (dispatch [:navigate-to :group-settings])))
  prepare-chat-settings)

(register-handler :set-chat-name
  (after (save-chat-property! :new-chat-name :name))
  (update-chat-property :new-chat-name :name))

(register-handler :set-chat-color
  (after (save-chat-property! :new-chat-color :color))
  (update-chat-property :new-chat-color :color))

(register-handler :clear-history
  (fn [db _]
    (clear-history (:current-chat-id db))))

(register-handler :group-settings
  (fn [db [_ k v]]
    (assoc-in db [:group-settings k] v)))
