(ns status-im.protocol.web3.transport
  (:require [status-im.protocol.web3.utils :as u]
            [cljs.spec.alpha :as s]
            [status-im.protocol.validation :refer-macros [valid?]]
            [taoensso.timbre :refer-macros [debug]]))

(s/def :shh/payload string?)
(s/def :shh/message
  (s/keys
   :req-un [:shh/payload :message/ttl :message/sig :message/topic]))

(defn post-message!
  [web3 message callback]
  {:pre [(valid? :shh/message message)]}
  (debug :post-message message)
  (let [shh      (u/shh web3)
        message' (assoc message
                        :powTarget 0.001
                        :powTime 1)]
    (.post shh (clj->js message') callback)))
