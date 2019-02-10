(ns status-im.data-store.realm.schemas.account.dapp-permissions)

(def v9 {:name       :dapp-permissions
         :primaryKey :dapp
         :properties {:dapp        :string
                      :permissions {:type     "string[]"
                                    :optional true}}})
