(ns status-im.data-store.realm.schemas.account.mailserver-topic)

(def v1 {:name       :mailserver-topic
         :primaryKey :topic
         :properties {:topic        :string
                      :chat-ids     :string
                      :last-request {:type :int :default 1}}})

(def v2
  (-> v1
      (assoc-in
       [:properties :gap-from]
       {:type     :int
        :optional true})
      (assoc-in
       [:properties :gap-to]
       {:type     :int
        :optional true})))

(def v3
  (update v2 :properties dissoc :gap-to :gap-from))
