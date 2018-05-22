(ns status-im.data-store.realm.schemas.base.v2.account
  (:require [status-im.data-store.realm.core :as core]
            [status-im.constants :as constants]
            [taoensso.timbre :as log]
            [goog.object :as object]))

(def schema {:name       :account
             :primaryKey :address
             :properties {:address               :string
                          :public-key            :string
                          :name                  {:type :string :optional true}
                          :email                 {:type :string :optional true}
                          :status                {:type :string :optional true}
                          :debug?                {:type :bool :default false}
                          :photo-path            :string
                          :signing-phrase        {:type :string}
                          :mnemonic              {:type :string}
                          :last-updated          {:type :int :default 0}
                          :last-sign-in          {:type :int :default 0}
                          :signed-up?            {:type    :bool
                                                  :default false}
                          :network               :string
                          :networks              {:type       :list
                                                  :objectType :network}
                          :settings              {:type :string}
                          :sharing-usage-data?   {:type :bool :default false}
                          :dev-mode?             {:type :bool :default false}
                          :seed-backed-up?       {:type :bool :default false}
                          :wallet-set-up-passed? {:type    :bool
                                                  :default false}}})

(defn migration [old-realm new-realm]
  (log/debug "migrating account schema v2")
  (let [accounts     (.objects old-realm "account")
        new-accounts (.objects new-realm "account")]
    (dotimes [i (.-length accounts)]
      (let [account     (aget accounts i)
            new-account (aget new-accounts i)
            settings    (core/deserialize (object/get account "settings"))]
        (aset new-account
              "settings"
              (core/serialize (assoc settings
                                     :wnode (:wnode (constants/default-account-settings)))))))))
