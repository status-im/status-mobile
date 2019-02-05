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

(def v3 (assoc-in v2 [:properties :name] {:type     :string
                                          :optional true}))

(def v4 (assoc-in v3 [:properties :fcm-token] {:type     :string
                                               :optional true}))
