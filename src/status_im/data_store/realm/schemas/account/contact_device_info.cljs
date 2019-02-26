(ns status-im.data-store.realm.schemas.account.contact-device-info)

(def v1 {:name :contact-device-info
         :primaryKey :id
         :properties {:id :string
                      :timestamp :int
                      :fcm-token :string}})
