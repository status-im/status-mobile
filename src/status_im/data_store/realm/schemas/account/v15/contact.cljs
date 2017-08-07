(ns status-im.data-store.realm.schemas.account.v15.contact
  (:require [taoensso.timbre :as log]))

(def schema {:name       :contact
             :primaryKey :whisper-identity
             :properties {:address          {:type :string :optional true}
                          :whisper-identity :string
                          :name             {:type :string :optional true}
                          :photo-path       {:type :string :optional true}
                          :last-updated     {:type :int :default 0}
                          :last-online      {:type :int :default 0}
                          :pending?         {:type :bool :default false}
                          :mixable?         {:type :bool :default false}
                          :status           {:type :string :optional true}
                          :fcm-token        {:type :string :optional true}
                          :public-key       {:type     :string
                                             :optional true}
                          :private-key      {:type     :string
                                             :optional true}
                          :dapp?            {:type    :bool
                                             :default false}
                          :dapp-url         {:type     :string
                                             :optional true}
                          :bot-url          {:type     :string
                                             :optional true}
                          :global-command   {:type     :command
                                             :optional true}
                          :commands         {:type       :list
                                             :objectType :command}
                          :responses        {:type       :list
                                             :objectType :command}
                          :dapp-hash        {:type     :int
                                             :optional true}
                          :debug?           {:type    :bool
                                             :default false}}})

(defn migration [old-realm new-realm]
  (log/debug "migrating contact schema v15")
  (let [new-contacts (.objects new-realm "contact")]
    (dotimes [i (.-length new-contacts)]
      (let [contact (aget new-contacts i)
            id      (aget contact "whisper-identity")]
        (when (or (= id "mailman")
                  (= id "transactor-group")
                  (= id "transactor-personal"))
          (aset contact "mixable?" true))))))
