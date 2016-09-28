(ns status-im.profile.validations
  (:require [cljs.spec :as s]
            [status-im.constants :refer [console-chat-id wallet-chat-id]]
            [clojure.string :as str]))

(def homoglyph-finder (js/require "homoglyph-finder"))

(defn correct-name? [username]
  (let [username (some-> username (str/trim))]
    (and (not (.isMatches homoglyph-finder username console-chat-id))
         (not (.isMatches homoglyph-finder username wallet-chat-id)))))

(defn correct-email? [email]
  (let [pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"]
    (or (str/blank? email)
        (and (string? email) (re-matches pattern email)))))

(s/def ::name correct-name?)
(s/def ::email correct-email?)

(s/def ::profile (s/keys :req-un [::name ::email]))
