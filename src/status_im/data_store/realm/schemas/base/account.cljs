(ns status-im.data-store.realm.schemas.base.account)

(def v1 {:name       :account
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
                      :wallet-set-up-passed? {:type :bool :default false}}})

(def v2 {:name       :account
         :primaryKey :address
         :properties {:address               :string
                      :public-key            :string
                      :name                  {:type :string :optional true}
                      :email                 {:type :string :optional true}
                      :status                {:type :string :optional true}
                      :debug?                {:type :bool :default false}
                      :photo-path            :string
                      :signing-phrase        {:type :string}
                      :mnemonic              {:type :string :optional true}
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

(def v3 {:name       :account
         :primaryKey :address
         :properties {:address               :string
                      :public-key            :string
                      :name                  {:type :string :optional true}
                      :email                 {:type :string :optional true}
                      :status                {:type :string :optional true}
                      :debug?                {:type :bool :default false}
                      :photo-path            :string
                      :signing-phrase        {:type :string}
                      :mnemonic              {:type :string :optional true}
                      :last-updated          {:type :int :default 0}
                      :last-sign-in          {:type :int :default 0}
                      :signed-up?            {:type    :bool
                                              :default false}
                      :network               :string
                      :networks              {:type       :list
                                              :objectType :network}
                      :last-request          {:type :int :optional true}
                      :settings              {:type :string}
                      :sharing-usage-data?   {:type :bool :default false}
                      :dev-mode?             {:type :bool :default false}
                      :seed-backed-up?       {:type :bool :default false}
                      :wallet-set-up-passed? {:type    :bool
                                              :default false}}})

(def v4 {:name       :account
         :primaryKey :address
         :properties {:address               :string
                      :public-key            :string
                      :name                  {:type :string :optional true}
                      :email                 {:type :string :optional true}
                      :status                {:type :string :optional true}
                      :debug?                {:type :bool :default false}
                      :photo-path            :string
                      :signing-phrase        {:type :string}
                      :mnemonic              {:type :string :optional true}
                      :last-updated          {:type :int :default 0}
                      :last-sign-in          {:type :int :default 0}
                      :signed-up?            {:type    :bool
                                              :default false}
                      :network               :string
                      :networks              {:type       :list
                                              :objectType :network}
                      :bootnodes             {:type       :list
                                              :objectType :bootnode}
                      :last-request          {:type :int :optional true}
                      :settings              {:type :string}
                      :sharing-usage-data?   {:type :bool :default false}
                      :dev-mode?             {:type :bool :default false}
                      :seed-backed-up?       {:type :bool :default false}
                      :wallet-set-up-passed? {:type    :bool
                                              :default false}}})

(def v6 {:name       :account
         :primaryKey :address
         :properties {:address                :string
                      :public-key             :string
                      :name                   {:type :string :optional true}
                      :email                  {:type :string :optional true}
                      :status                 {:type :string :optional true}
                      :debug?                 {:type :bool :default false}
                      :photo-path             :string
                      :signing-phrase         {:type :string}
                      :mnemonic               {:type :string :optional true}
                      :last-updated           {:type :int :default 0}
                      :last-sign-in           {:type :int :default 0}
                      :signed-up?             {:type    :bool
                                               :default false}
                      :network                :string
                      :networks               {:type       :list
                                               :objectType :network}
                      :bootnodes              {:type       :list
                                               :objectType :bootnode}
                      :last-request           {:type :int :optional true}
                      :settings               {:type :string}
                      :sharing-usage-data?    {:type :bool :default false}
                      :dev-mode?              {:type :bool :default false}
                      :seed-backed-up?        {:type :bool :default false}
                      :wallet-set-up-passed?  {:type    :bool
                                               :default false}
                      :mainnet-warning-shown? {:type    :bool
                                               :default false}}})

(def v7 {:name       :account
         :primaryKey :address
         :properties {:address                :string
                      :public-key             :string
                      :name                   {:type :string :optional true}
                      :email                  {:type :string :optional true}
                      :status                 {:type :string :optional true}
                      :debug?                 {:type :bool :default false}
                      :photo-path             :string
                      :signing-phrase         {:type :string}
                      :mnemonic               {:type :string :optional true}
                      :last-updated           {:type :int :default 0}
                      :last-sign-in           {:type :int :default 0}
                      :signed-up?             {:type    :bool
                                               :default false}
                      :network                :string
                      :networks               {:type       :list
                                               :objectType :network}
                      :bootnodes              {:type       :list
                                               :objectType :bootnode}
                      :last-request           {:type :int :optional true}
                      :settings               {:type :string}
                      :dev-mode?              {:type :bool :default false}
                      :seed-backed-up?        {:type :bool :default false}
                      :wallet-set-up-passed?  {:type    :bool
                                               :default false}
                      :mainnet-warning-shown? {:type    :bool
                                               :default false}}})

;;NOTE(yenda): this was a mistake made because of the previous
;;way realm migrations were specified
(def v8 v4)

(def v10 {:name       :account
          :primaryKey :address
          :properties {:address                :string
                       :public-key             :string
                       :name                   {:type :string :optional true}
                       :email                  {:type :string :optional true}
                       :status                 {:type :string :optional true}
                       :debug?                 {:type :bool :default false}
                       :photo-path             :string
                       :signing-phrase         {:type :string}
                       :mnemonic               {:type :string :optional true}
                       :last-updated           {:type :int :default 0}
                       :last-sign-in           {:type :int :default 0}
                       :signed-up?             {:type    :bool
                                                :default false}
                       :network                :string
                       :networks               {:type       :list
                                                :objectType :network}
                       :bootnodes              {:type       :list
                                                :objectType :bootnode}
                       :last-request           {:type :int :optional true}
                       :settings               {:type :string}
                       :dev-mode?              {:type :bool :default false}
                       :seed-backed-up?        {:type :bool :default false}
                       :wallet-set-up-passed?  {:type    :bool
                                                :default false}
                       :mainnet-warning-shown? {:type    :bool
                                                :default false}}})

(def v11 (assoc-in v10
                   [:properties :installation-id]
                   {:type :string}))

(def v12 (assoc-in v11
                   [:properties :extensions]
                   {:type       :list
                    :objectType :extension}))