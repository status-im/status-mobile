(ns status-im.new-group.validations
  (:require [cljs.spec :as s]
            [status-im.utils.phone-number :refer [valid-mobile-number?]]
            [status-im.constants :refer [console-chat-id wallet-chat-id]]
            [clojure.string :as str]
            [status-im.utils.homoglyph :as h]))

(defn not-illegal-name? [username]
  (let [username (some-> username (str/trim))]
    (and (not (h/matches username console-chat-id))
         (not (h/matches username wallet-chat-id)))))

(s/def ::not-empty-string (s/and string? not-empty))
(s/def ::not-illegal-name not-illegal-name?)

(s/def ::name (s/and ::not-empty-string
                     ::not-illegal-name))

(s/def ::topic (s/and ::not-empty-string
                      ::not-illegal-name
                      (partial re-matches #"[a-z0-9\-]+")))
