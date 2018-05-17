(ns status-im.data-store.realm.schemas.base.v1.account)

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
