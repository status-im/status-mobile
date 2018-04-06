(ns status-im.data-store.realm.schemas.base.v1.network
  (:require [taoensso.timbre :as log]))

(def schema {:name       :network
             :primaryKey :id
             :properties {:id      :string
                          :name    {:type     :string
                                    :optional true}
                          :config  {:type     :string
                                    :optional true}
                          :rpc-url {:type     :string
                                    :optional true}}})
