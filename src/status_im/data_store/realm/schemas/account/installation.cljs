(ns status-im.data-store.realm.schemas.account.installation)

(def v1 {:name       :installation
         :primaryKey :installation-id
         :properties {:installation-id :string
                      :confirmed?      :bool}})
