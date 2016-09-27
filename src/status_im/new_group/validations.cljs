(ns status-im.new-group.validations
  (:require [cljs.spec :as s]
            [status-im.utils.phone-number :refer [valid-mobile-number?]]
            [status-im.constants :refer [console-chat-id wallet-chat-id]]
            [clojure.string :as str]))

(def homoglyph-finder (js/require "homoglyph-finder"))

(defn not-illegal-name? [username]
  (let [username (some-> username (str/trim))]
    (and (not (.isMatches homoglyph-finder username console-chat-id))
         (not (.isMatches homoglyph-finder username wallet-chat-id)))))

(s/def ::not-empty-string (s/and string? not-empty))
(s/def ::not-illegal-name not-illegal-name?)

(s/def ::name (s/and ::not-empty-string
                     ::not-illegal-name))