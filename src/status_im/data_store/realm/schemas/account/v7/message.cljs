(ns status-im.data-store.realm.schemas.account.v7.message
  (:require [taoensso.timbre :as log]))

(def schema {:name       :message
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

(defn migration [old-realm new-realm]
  (log/debug "migrating messages schema v7")
  (let [messages (.objects new-realm "message")]
    (dotimes [i (.-length messages)]
      (js-delete (aget messages i) "user-statuses"))))
