(ns status-im.data-store.realm.schemas.account.v5.command-parameter
  (:require [taoensso.timbre :as log]))

(def schema {:name       :command-parameter
             :properties {:name {:type :string}
                          :type {:type :string}}})

(defn migration [_ _]
  (log/debug "migrating command-parameter schema"))
