(ns status-im.data-store.realm.schemas.account.v1.contact-group
  (:require [taoensso.timbre :as log]))

(def schema {:name       :contact-group
             :primaryKey :group-id
             :properties {:group-id         :string
                          :name             :string
                          :timestamp        :int
                          :order            :int
                          :pending?         {:type :bool :default false}
                          :contacts         {:type       :list
                                             :objectType :group-contact}}})
