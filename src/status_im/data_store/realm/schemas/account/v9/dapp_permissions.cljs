(ns status-im.data-store.realm.schemas.account.v9.dapp-permissions)

(def schema {:name       :dapp-permissions
             :primaryKey :dapp
             :properties {:dapp        :string
                          :permissions {:type     "string[]"
                                        :optional true}}})