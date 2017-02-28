(ns status-im.data-store.realm.schemas.account.v5.command
  (:require [taoensso.timbre :as log]))

(def schema {:name       :command
             :properties {:description         {:type     :string
                                                :optional true}
                          :color               {:type     :string
                                                :optional true}
                          :name                {:type :string}
                          :params              {:type       :list
                                                :objectType :command-parameter}
                          :title               {:type     :string
                                                :optional true}
                          :has-handler         {:type    :bool
                                                :default true}
                          :fullscreen          {:type    :bool
                                                :default true}
                          :suggestions-trigger {:type    :string
                                                :default "on-change"}}})

(defn migration [_ _]
  (log/debug "migrating chat-contact schema"))
