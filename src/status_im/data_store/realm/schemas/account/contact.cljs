(ns status-im.data-store.realm.schemas.account.contact)

(def v1 {:name       :contact
         :primaryKey :whisper-identity
         :properties {:address          {:type :string :optional true}
                      :whisper-identity :string
                      :name             {:type :string :optional true}
                      :photo-path       {:type :string :optional true}
                      :last-updated     {:type :int :default 0}
                      :last-online      {:type :int :default 0}
                      :pending?         {:type :bool :default false}
                      :hide-contact?    {:type :bool :default false}
                      :status           {:type :string :optional true}
                      :fcm-token        {:type :string :optional true}
                      :description      {:type :string :optional true}
                      :public-key       {:type     :string
                                         :optional true}
                      :dapp?            {:type    :bool
                                         :default false}
                      :dapp-url         {:type     :string
                                         :optional true}
                      :bot-url          {:type     :string
                                         :optional true}
                      :dapp-hash        {:type     :int
                                         :optional true}
                      :debug?           {:type    :bool
                                         :default false}}})

(def v2 {:name       :contact
         :primaryKey :whisper-identity
         :properties {:address          {:type :string :optional true}
                      :whisper-identity :string
                      :name             {:type :string :optional true}
                      :photo-path       {:type :string :optional true}
                      :last-updated     {:type :int :default 0}
                      :last-online      {:type :int :default 0}
                      :pending?         {:type :bool :default false}
                      :hide-contact?    {:type :bool :default false}
                      :status           {:type :string :optional true}
                      :fcm-token        {:type :string :optional true}
                      :description      {:type :string :optional true}
                      :public-key       {:type     :string
                                         :optional true}
                      :dapp?            {:type    :bool
                                         :default false}
                      :dapp-url         {:type     :string
                                         :optional true}
                      :bot-url          {:type     :string
                                         :optional true}
                      :dapp-hash        {:type     :int
                                         :optional true}
                      :debug?           {:type    :bool
                                         :default false}
                      :tags             {:type     "string[]"}}})

(def v3 {:name       :contact
         :primaryKey :public-key
         :properties {:address          {:type :string :optional true}
                      :name             {:type :string :optional true}
                      :photo-path       {:type :string :optional true}
                      :last-updated     {:type :int :default 0}
                      :last-online      {:type :int :default 0}
                      :pending?         {:type :bool :default false}
                      :hide-contact?    {:type :bool :default false}
                      :status           {:type :string :optional true}
                      :fcm-token        {:type :string :optional true}
                      :description      {:type :string :optional true}
                      :public-key       :string
                      :dapp?            {:type    :bool
                                         :default false}
                      :dapp-url         {:type     :string
                                         :optional true}
                      :bot-url          {:type     :string
                                         :optional true}
                      :dapp-hash        {:type     :int
                                         :optional true}
                      :debug?           {:type    :bool
                                         :default false}
                      :tags             {:type     "string[]"}}})

(def v4 {:name       :contact
         :primaryKey :public-key
         :properties {:address          {:type :string :optional true}
                      :name             {:type :string :optional true}
                      :photo-path       {:type :string :optional true}
                      :last-updated     {:type :int :default 0}
                      :last-online      {:type :int :default 0}
                      :blocked?         {:type :bool :default false}
                      :pending?         {:type :bool :default false}
                      :hide-contact?    {:type :bool :default false}
                      :status           {:type :string :optional true}
                      :fcm-token        {:type :string :optional true}
                      :description      {:type :string :optional true}
                      :public-key       :string
                      :tags             {:type     "string[]"}}})

(def v5 (assoc-in v4 [:properties :device-info]
                  {:type       :list
                   :objectType :contact-device-info}))

(def v6 {:name       :contact
         :primaryKey :public-key
         :properties {:address          {:type :string :optional true}
                      :name             {:type :string :optional true}
                      :photo-path       {:type :string :optional true}
                      :last-updated     {:type :int :default 0}
                      :last-online      {:type :int :default 0}
                      :blocked?         {:type :bool :default false}
                      :pending?         {:type :bool :default false}
                      :status           {:type :string :optional true}
                      :fcm-token        {:type :string :optional true}
                      :description      {:type :string :optional true}
                      :public-key       :string
                      :tags             {:type "string[]"}
                      :device-info      {:type       :list
                                         :objectType :contact-device-info}}})

(def v7 {:name       :contact
         :primaryKey :public-key
         :properties {:address          {:type :string :optional true}
                      :name             {:type :string :optional true}
                      :photo-path       {:type :string :optional true}
                      :last-updated     {:type :int :default 0}
                      :last-online      {:type :int :default 0}
                      :fcm-token        {:type :string :optional true}
                      :public-key       :string
                      :tags             {:type "string[]"}
                      :system-tags      {:type "string[]"}
                      :device-info      {:type       :list
                                         :objectType :contact-device-info}}})

(def v8 (assoc-in v7 [:properties :tribute-to-talk] {:type :string :optional true}))
