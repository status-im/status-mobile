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

(def v13 (assoc-in v12
                   [:properties :desktop-notifications?]
                   {:type :bool :default false}))

(def v14 (assoc-in v13
                   [:properties :desktop-alpha-release-warning-shown?]
                   {:type :bool :default false}))

(def v15 (update v14 :properties dissoc :email))

(def v16 (assoc-in v15
                   [:properties :keycard-instance-uid]
                   {:type :string :optional true}))

(def v17 (update v16 :properties merge {:keycard-pairing
                                        {:type :string :optional true}
                                        :keycard-paired-on
                                        {:type :int :optional true}}))
(def v18 (assoc-in v17
                   [:properties :installation-name]
                   {:type :string :optional true}))

(def v19 (update v18 :properties merge {:stickers
                                        {:type "string[]" :optional true}
                                        :recent-stickers
                                        {:type "string[]" :optional true}}))

(def v20 (assoc-in v19
                   [:properties :last-published-contact-code]
                   {:type :int :default 0}))

(def v21 (update v20 :properties merge
                 {:syncing-on-mobile-network? {:type :bool :default false}
                  :remember-syncing-choice?   {:type :bool :default false}}))

(def v22 (update (update v21 :properties dissoc :mainnet-warning-shown?)
                 :properties merge
                 {:mainnet-warning-shown-version {:type :string :optional true}}))

(def v23 (assoc-in v22
                   [:properties :keycard-key-uid]
                   {:type :string :optional true}))
