(ns status-im.group-settings.subs
  (:require [re-frame.core :refer [reg-sub]]
            [status-im.constants :refer [max-chat-name-length]]))

(reg-sub :selected-participant
  (fn [db]
    (let [identity (first (:selected-participants db))]
      (get-in db [:contacts/contacts identity]))))

(defn get-chat-name-validation-messages [chat-name]
  (filter some?
          (list (when (zero? (count chat-name))
                  "Chat name can't be empty")
                (when (< max-chat-name-length (count chat-name))
                  "Chat name is too long"))))

(reg-sub :new-chat-name-validation-messages
  (fn [db]
    (let [chat-name (:new-chat-name db)]
      (get-chat-name-validation-messages chat-name))))

(reg-sub :new-chat-name-valid?
  (fn [db]
    (let [chat-name (:new-chat-name db)]
      (zero? (count (get-chat-name-validation-messages chat-name))))))
