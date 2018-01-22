(ns status-im.data-store.realm.schemas.base.v3.account
  (:require [goog.object :as object]
            [taoensso.timbre :as log]
            [status-im.utils.signing-phrase.core :as signing-phrase]))

(def schema {:name       :account
             :primaryKey :address
             :properties {:address             :string
                          :public-key          :string
                          :updates-public-key  {:type     :string
                                                :optional true}
                          :updates-private-key {:type     :string
                                                :optional true}
                          :name                {:type :string :optional true}
                          :phone               {:type :string :optional true}
                          :email               {:type :string :optional true}
                          :status              {:type :string :optional true}
                          :debug?              {:type :bool :default false}
                          :photo-path          :string
                          :signing-phrase      {:type :string}
                          :last-updated        {:type :int :default 0}
                          :signed-up?          {:type    :bool
                                                :default false}
                          :network             :string}})

(defn migration [_old-realm new-realm]
  (log/debug "migrating account schema v3")
  (let [accounts (.objects new-realm "account")]
    (dotimes [i (.-length accounts)]
      (let [account (aget accounts i)
            phrase  (object/get account "signing-phrase")]
        (when (empty? phrase)
          (log/debug (js->clj account))
          (aset account "signing-phrase" (signing-phrase/generate)))))))

