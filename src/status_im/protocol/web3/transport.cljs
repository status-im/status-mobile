(ns status-im.protocol.web3.transport
  (:require [status-im.protocol.web3.utils :as u]
            [cljs.spec :as s]
            [status-im.protocol.validation :refer-macros [valid?]]
            [taoensso.timbre :refer-macros [debug]]))

(s/def :shh/payload string?)
(s/def :shh/message
  (s/keys
    :req-un [:shh/payload :message/ttl :message/from :message/topics]
    :opt-un [:message/to]))

(defn post-message!
  [web3 {:keys [topics from to] :as message} callback]
  {:pre [(valid? :shh/message message)]}
  (debug :post-message message)
  (let [topic      (first topics)
        shh        (u/shh web3)
        encrypted? (boolean to)
        message'   (if encrypted?
                     message
                     (assoc message :keyname topic))
        do-post    (fn [] (.post shh (clj->js message') callback))]
    (if encrypted?
      (do-post)
      (.hasSymKey
        shh topic
        (fn [_ res]
          (if-not res
            (.addSymKey
              shh topic u/status-key-data
              (fn [error _]
                (when-not error (do-post))))
            (do-post)))))))
