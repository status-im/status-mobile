(ns status-im.chat.new-public-chat.db
  (:require [cljs.spec.alpha :as spec]
            [status-im.constants :refer [console-chat-id wallet-chat-id]]
            [clojure.string :as string]
            [status-im.utils.homoglyph :as utils]))

(defn legal-name? [username]
  (let [username (some-> username string/trim)]
    (and (not (utils/matches username console-chat-id))
         (not (utils/matches username wallet-chat-id)))))

(spec/def ::legal-name legal-name?)
(spec/def ::not-empty-string (spec/and string? not-empty))

(spec/def ::name (spec/and ::not-empty-string
                           ::legal-name))

(spec/def ::topic (spec/and ::not-empty-string
                            ::legal-name
                            (partial re-matches #"[a-z0-9\-]+")))
