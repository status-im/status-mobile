(ns status-im.data-store.realm.schemas.account.v8.browser
  (:require [taoensso.timbre :as log]))

(def schema {:name       :browser
             :primaryKey :browser-id
             :properties {:browser-id    :string
                          :name          :string
                          :timestamp     :int
                          :dapp?         {:type    :bool
                                          :default false}
                          :history-index {:type     :int
                                          :optional true}
                          :history       {:type     "string[]"
                                          :optional true}}})

(defn migration [old-realm new-realm]
  (log/debug "migrating browser schema v8")
  (let [browsers     (.objects new-realm "browser")
        old-browsers (.objects old-realm "browser")]
    (dotimes [i (.-length browsers)]
      (let [browser     (aget browsers i)
            old-browser (aget old-browsers i)
            url         (aget old-browser "url")]
        (aset browser "history-index" 0)
        (aset browser "history" (clj->js [url]))))))