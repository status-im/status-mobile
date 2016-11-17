(ns status-im.data-store.realm.schemas.account.v4.chat
  (:require [taoensso.timbre :as log]
            [status-im.components.styles :refer [default-chat-color]]))

(def schema {:name       :chat
             :primaryKey :chat-id
             :properties {:chat-id          :string
                          :name             :string
                          :color            {:type    :string
                                             :default default-chat-color}
                          :group-chat       {:type    :bool
                                             :indexed true}
                          :is-active        :bool
                          :timestamp        :int
                          :contacts         {:type       :list
                                             :objectType :chat-contact}
                          :dapp-url         {:type     :string
                                             :optional true}
                          :dapp-hash        {:type     :int
                                             :optional true}
                          :removed-at       {:type     :int
                                             :optional true}
                          :last-message-id  :string
                          :public-key       {:type     :string
                                             :optional true}
                          :private-key      {:type     :string
                                             :optional true}
                          :clock-value      {:type    :int
                                             :default 0}
                          :pending-contact? {:type    :bool
                                             :default false}
                          :contact-info     {:type     :string
                                             :optional true}}})

(defn migration [_ new-realm]
  (let [new-objs (.objects new-realm "chat")]
    (dotimes [i (.-length new-objs)]
      (aset (aget new-objs i) "pending-contact?" false))))
