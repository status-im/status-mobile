(ns syng-im.models.messages
  (:require [syng-im.persistence.realm :as r]
            [cljs.reader :refer [read-string]]
            [syng-im.utils.random :refer [timestamp]]
            [syng-im.db :as db]
            [syng-im.utils.logging :as log]))

(defn save-message [chat-id {:keys [from to msg-id content content-type outgoing] :or {outgoing false
                                                                                       to       nil} :as msg}]
  (log/debug "save-message" chat-id msg)
  (when-not (r/exists? :msgs :msg-id msg-id)
    (r/write
      (fn []
        (r/create :msgs {:chat-id         chat-id
                         :msg-id          msg-id
                         :from            from
                         :to              to
                         :content         content
                         :content-type    content-type
                         :outgoing        outgoing
                         :timestamp       (timestamp)
                         :delivery-status nil} true)))))

(defn get-messages [chat-id]
  (r/sorted (r/get-by-field :msgs :chat-id chat-id) :timestamp :desc))

(defn message-by-id [msg-id]
  (r/single-cljs (r/get-by-field :msgs :msg-id msg-id)))

(defn update-message! [{:keys [msg-id] :as msg}]
  (log/debug "update-message!" msg)
  (r/write
    (fn []
      (when (r/exists? :msgs :msg-id msg-id)
        (r/create :msgs msg true)))))

(comment

  (update-message! {:msg-id          "1459175391577-a2185a35-5c49-5a6b-9c08-6eb5b87ceb7f"
                    :delivery-status "seen2"})

  (r/get-by-field :msgs :msg-id "1459175391577-a2185a35-5c49-5a6b-9c08-6eb5b87ceb7f")


  (save-message "0x040028c500ff086ecf1cfbb3c1a7240179cde5b86f9802e6799b9bbe9cdd7ad1b05ae8807fa1f9ed19cc8ce930fc2e878738c59f030a6a2f94b3522dc1378ff154"
                {:msg-id       "153"
                 :content      "hello!"
                 :content-type "text/plain"})

  (get-messages* "0x040028c500ff086ecf1cfbb3c1a7240179cde5b86f9802e6799b9bbe9cdd7ad1b05ae8807fa1f9ed19cc8ce930fc2e878738c59f030a6a2f94b3522dc1378ff154")

  (get-messages "0x0479a5ed1f38cadfad1db6cd56c4b659b0ebe052bbe9efa950f6660058519fa4ca6be2dda66afa80de96ab00eb97a2605d5267a1e8f4c2a166ab551f6826608cdd")

  (doseq [msg (get-messages* "0x043df89d36f6e3d8ade18e55ac3e2e39406ebde152f76f2f82d674681d59319ffd9880eebfb4f5f8d5c222ec485b44d6e30ba3a03c96b1c946144fdeba1caccd43")]
    (r/delete msg))

  @re-frame.db/app-db

  )
