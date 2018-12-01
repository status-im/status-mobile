(ns status-im.data-store.realm.schemas.account.message)

(def v1 {:name       :message
         :primaryKey :message-id
         :properties {:message-id       :string
                      :from             :string
                      :to               {:type     :string
                                         :optional true}
                      :content          :string ; TODO make it ArrayBuffer
                      :content-type     :string
                      :username         {:type     :string
                                         :optional true}
                      :timestamp        :int
                      :chat-id          {:type    :string
                                         :indexed true}
                      :outgoing         :bool
                      :retry-count      {:type    :int
                                         :default 0}
                      :message-type     {:type     :string
                                         :optional true}
                      :message-status   {:type     :string
                                         :optional true}
                      :user-statuses    {:type       :list
                                         :objectType :user-status}
                      :clock-value      {:type    :int
                                         :default 0}
                      :show?            {:type    :bool
                                         :default true}}})

(def v7 {:name       :message
         :primaryKey :message-id
         :properties {:message-id       :string
                      :from             :string
                      :to               {:type     :string
                                         :optional true}
                      :content          :string ; TODO make it ArrayBuffer
                      :content-type     :string
                      :username         {:type     :string
                                         :optional true}
                      :timestamp        :int
                      :chat-id          {:type    :string
                                         :indexed true}
                      :outgoing         :bool
                      :retry-count      {:type    :int
                                         :default 0}
                      :message-type     {:type     :string
                                         :optional true}
                      :message-status   {:type     :string
                                         :optional true}
                      :clock-value      {:type    :int
                                         :default 0}
                      :show?            {:type    :bool
                                         :default true}}})

(def v8
  (-> v7
      (assoc-in [:properties :old-message-id]
                {:type    :string
                 :indexed true})))
