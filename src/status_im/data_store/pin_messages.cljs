(ns status-im.data-store.pin-messages
  (:require [clojure.set :as clojure.set]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.utils.fx :as fx]
            [taoensso.timbre :as log]
            [status-im.data-store.messages :as messages]))

(defn <-rpc [message]
  (-> message
      (merge (messages/<-rpc (message :message)))
      (clojure.set/rename-keys {:pinnedAt :pinned-at
                                :pinnedBy :pinned-by})
      (dissoc :message)))

(defn pinned-message-by-chat-id-rpc [chat-id
                                     cursor
                                     limit
                                     on-success
                                     on-error]
  {::json-rpc/call [{:method     "wakuext_chatPinnedMessages"
                     :params     [chat-id cursor limit]
                     :on-success (fn [result]
                                   (let [result (clojure.set/rename-keys result {:pinnedMessages :pinned-messages})]
                                     (on-success (update result :pinned-messages #(map <-rpc %)))))
                     :on-error on-error}]})

(fx/defn send-pin-message [cofx pin-message]
  {::json-rpc/call [{:method "wakuext_sendPinMessage"
                     :params [(messages/->rpc pin-message)]
                     :on-success #(log/debug "successfully pinned message" pin-message)
                     :on-error #(log/error "failed to pin message" % pin-message)}]})
