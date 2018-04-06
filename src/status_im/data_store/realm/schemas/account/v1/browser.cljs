(ns status-im.data-store.realm.schemas.account.v1.browser)

(def schema {:name       :browser
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
