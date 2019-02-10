(ns status-im.data-store.realm.schemas.account.contact-recovery)

(def v1 {:name       :contact-recovery
         :primaryKey :id
         :properties {:id        :string
                      :timestamp {:type     :int
                                  :optional true}}})
