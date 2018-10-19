(ns status-im.data-store.realm.schemas.base.bootnode)

(def v4 {:name       :bootnode
         :primaryKey :id
         :properties {:id      :string
                      :name    {:type     :string}
                      :chain   {:type     :string}
                      :address {:type     :string}}})
