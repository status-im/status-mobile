(ns status-im.data-store.realm.schemas.account.browser)

(def v1 {:name       :browser
         :primaryKey :browser-id
         :properties {:browser-id :string
                      :name       :string
                      :timestamp  :int
                      :dapp?      {:type    :bool
                                   :default false}
                      :url        {:type     :string
                                   :optional true}
                      :contact    {:type     :string
                                   :optional true}}})

(def v8 {:name       :browser
         :primaryKey :browser-id
         :properties {:browser-id    :string
                      :name          :string
                      :timestamp     :int
                      :dapp?         {:type    :bool
                                      :default false}
                      :history-index {:type     :int
                                      :optional true}
                      :history       {:type     "string[]"
                                      :optional true}}})
