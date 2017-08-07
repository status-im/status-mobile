(ns status-im.data-store.realm.schemas.account.v15.request
  (:require [taoensso.timbre :as log]))

(def schema {:name       :request
             :properties {:message-id :string
                          :chat-id    :string
                          :bot        {:type     :string
                                       :optional true}
                          :type       :string
                          :status     {:type    :string
                                       :default "open"}
                          :added      :date}})

(defn migration [_ _]
  (log/debug "migrating request schema"))
