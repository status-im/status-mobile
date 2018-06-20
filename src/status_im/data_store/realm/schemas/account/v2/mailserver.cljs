(ns status-im.data-store.realm.schemas.account.v2.mailserver
  (:require [taoensso.timbre :as log]))

(def schema {:name       :mailserver
             :primaryKey :id
             :properties {:id        :string
                          :name      {:type     :string}
                          :address   {:type     :string}
                          :password  {:type     :string
                                      :optional true}
                          :chain     {:type     :string}}})
