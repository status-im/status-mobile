(ns status-im.data-store.realm.schemas.account.v6.command
  (:require [taoensso.timbre :as log]))

(def schema {:name       :command
             :properties {:description         {:type     :string
                                                :optional true}
                          :color               {:type     :string
                                                :optional true}
                          :name                {:type :string}
                          :icon                {:type     :string
                                                :optional true}
                          :params              {:type       :list
                                                :objectType :command-parameter}
                          :title               {:type     :string
                                                :optional true}
                          :has-handler         {:type    :bool
                                                :default true}
                          :fullscreen          {:type    :bool
                                                :default true}
                          :suggestions-trigger {:type    :string
                                                :default "on-change"}
                          :sequential-params   {:type    :bool
                                                :default false}}})

(defn migration [old-realm new-realm]
  (log/debug "migrating chat-contact schema v6"))
