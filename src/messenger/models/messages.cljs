(ns messenger.models.messages
  (:require [messenger.persistence.realm :as r]))

(defn save-message [from {:keys [msg-id] :as msg}]
  (when-not (r/exists? :msgs :msg-id msg-id)
    (r/write
      (fn []
        (r/create :msgs {:msg-id  msg-id
                         :chat-id from
                         :msg     (with-out-str (pr msg))} true)))))