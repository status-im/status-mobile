(ns messenger.models.messages
  (:require [messenger.persistence.realm :as r]
            [syng-im.utils.random :refer [timestamp]]))

(defn save-message [from {:keys [msg-id] :as msg}]
  (when-not (r/exists? :msgs :msg-id msg-id)
    (r/write
      (fn []
        (r/create :msgs {:msg-id    msg-id
                         :chat-id   from
                         :timestamp (timestamp)
                         :msg       (with-out-str (pr msg))} true)))))

(defn get-messages [chat-id]
  (-> (r/get-by-field :msgs :chat-id chat-id)
      (r/sorted :timestamp)
      (r/page 0 10)))

(comment

  (save-message "0x040028c500ff086ecf1cfbb3c1a7240179cde5b86f9802e6799b9bbe9cdd7ad1b05ae8807fa1f9ed19cc8ce930fc2e878738c59f030a6a2f94b3522dc1378ff154"
                {:msg-id  "23456"
                 :content "hi!"})
  (get-messages "0x040028c500ff086ecf1cfbb3c1a7240179cde5b86f9802e6799b9bbe9cdd7ad1b05ae8807fa1f9ed19cc8ce930fc2e878738c59f030a6a2f94b3522dc1378ff154")

  )