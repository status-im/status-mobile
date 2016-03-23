(ns messenger.models.messages
  (:require [messenger.persistence.realm :as r]
            [cljs.reader :refer [read-string]]
            [syng-im.utils.random :refer [timestamp]]))

(defn save-message [chat-id {:keys [from to msg-id content content-type outgoing] :or {outgoing false} :as msg}]
  (when-not (r/exists? :msgs :msg-id msg-id)
    (r/write
      (fn []
        (r/create :msgs {:chat-id      chat-id
                         :msg-id       msg-id
                         :from         from
                         :to           to
                         :content      content
                         :content-type content-type
                         :outgoing     outgoing
                         :timestamp    (timestamp)} true)))))

(defn get-messages* [chat-id]
  (-> (r/get-by-field :msgs :chat-id chat-id)
      (r/sorted :timestamp :desc)
      (r/page 0 10)))

(defn get-messages [chat-id]
  (-> (get-messages* chat-id)
      (js->clj :keywordize-keys true)))

(comment

  (save-message "0x040028c500ff086ecf1cfbb3c1a7240179cde5b86f9802e6799b9bbe9cdd7ad1b05ae8807fa1f9ed19cc8ce930fc2e878738c59f030a6a2f94b3522dc1378ff154"
                {:msg-id       "153"
                 :content      "hello!"
                 :content-type "text/plain"})

  (get-messages* "0x040028c500ff086ecf1cfbb3c1a7240179cde5b86f9802e6799b9bbe9cdd7ad1b05ae8807fa1f9ed19cc8ce930fc2e878738c59f030a6a2f94b3522dc1378ff154")

  (get-messages "0x043df89d36f6e3d8ade18e55ac3e2e39406ebde152f76f2f82d674681d59319ffd9880eebfb4f5f8d5c222ec485b44d6e30ba3a03c96b1c946144fdeba1caccd43")

  (doseq [msg (get-messages* "0x043df89d36f6e3d8ade18e55ac3e2e39406ebde152f76f2f82d674681d59319ffd9880eebfb4f5f8d5c222ec485b44d6e30ba3a03c96b1c946144fdeba1caccd43")]
    (r/delete msg))

  )