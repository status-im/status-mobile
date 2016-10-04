(ns status-im.data-store.realm.schemas.account.v1.request
  (:require [taoensso.timbre :as log]))

(def schema {:name       :request
             :properties {:message-id :string
                          :chat-id    :string
                          :type       :string
                          :status     {:type    :string
                                       :default "open"}
                          :added      :date}})

(defn migration [old-realm new-realm]
  (log/debug "migrating request schema"))