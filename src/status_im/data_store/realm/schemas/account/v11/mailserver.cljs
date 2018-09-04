(ns status-im.data-store.realm.schemas.account.v11.mailserver
  (:require [taoensso.timbre :as log]))

(def schema {:name       :mailserver
             :primaryKey :id
             :properties {:id        :string
                          :name      {:type     :string}
                          :address   {:type     :string}
                          :password  {:type     :string
                                      :optional true}
                          :fleet     {:type     :string}}})

(defn migration [old-realm new-realm]
  (log/debug "migrating mailservers schema v10")
  (let [mailservers     (.objects new-realm "mailserver")]
    (dotimes [i (.-length mailservers)]
      (aset (aget mailservers i) "fleet" "eth.beta"))))
