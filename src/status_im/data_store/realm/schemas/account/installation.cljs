(ns status-im.data-store.realm.schemas.account.installation)

(def v1 {:name       :installation
         :primaryKey :installation-id
         :properties {:installation-id :string
                      :confirmed?      :bool}})

(def v2 {:name       :installation
         :primaryKey :installation-id
         :properties {:installation-id :string
                      :device-type     {:type     :string
                                        :optional true}
                      :last-paired     {:type     :int
                                        :optional true}
                      :has-bundle?     {:type     :bool
                                        :optional true
                                        :default  false}
                      :enabled?        {:type     :bool
                                        :optional true
                                        :default  false}}})
