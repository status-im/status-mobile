(ns status-im.data-store.realm.schemas.account.v1.contact
  (:require [taoensso.timbre :as log]))

(def schema {:name       :contact
             :primaryKey :whisper-identity
             :properties {:address           {:type "string" :optional true}
                          :whisper-identity  "string"
                          :name              {:type "string" :optional true}
                          :photo-path        {:type "string" :optional true}
                          :last-updated      {:type "int" :default 0}
                          :last-online       {:type "int" :default 0}
                          :pending           {:type "bool" :default false}
                          :status            {:type "string" :optional true}
                          :public-key        {:type     :string
                                              :optional true}
                          :private-key       {:type     :string
                                              :optional true}
                          :keys-last-updated {:type "int" :default 0}
                          :dapp?             {:type    :bool
                                              :default false}
                          :dapp-url          {:type     :string
                                              :optional true}
                          :dapp-hash         {:type     :int
                                              :optional true}}})

(defn migration [_ _]
  (log/debug "migrating contact schema"))
