(ns status-im.data-store.realm.schemas.account.v9.command-parameter
  (:require [taoensso.timbre :as log]))

(def schema {:name       :command-parameter
             :properties {:name        {:type :string}
                          :type        {:type :string}
                          :placeholder {:type     :string
                                        :optional true}}})
