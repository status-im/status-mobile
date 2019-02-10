(ns status-im.data-store.realm.schemas.account.mailserver)

(def v2 {:name       :mailserver
         :primaryKey :id
         :properties {:id        :string
                      :name      {:type     :string}
                      :address   {:type     :string}
                      :password  {:type     :string
                                  :optional true}
                      :chain     {:type     :string}}})

(def v11 {:name       :mailserver
          :primaryKey :id
          :properties {:id        :string
                       :name      {:type     :string}
                       :address   {:type     :string}
                       :password  {:type     :string
                                   :optional true}
                       :fleet     {:type     :string}}})
