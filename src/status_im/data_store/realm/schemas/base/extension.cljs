(ns status-im.data-store.realm.schemas.base.extension)

(def v12 {:name       :extension
          :primaryKey :id
          :properties {:id      :string
                       :name    {:type :string}
                       :url     {:type :string}
                       :active? {:type    :bool
                                 :default true}
                       :data    {:type     :string
                                 :optional true}}})
