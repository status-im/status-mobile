(ns status-im.profile.validations
  (:require [cljs.spec :as s]
            [status-im.constants :refer [console-chat-id wallet-chat-id]]
            [status-im.chat.constants :as chat-consts]
            [clojure.string :as str]
            [status-im.utils.homoglyph :as h]))

(defn correct-name? [username]
  (let [username (some-> username (str/trim))]
    (every? false?
      [(str/blank? username)
       (h/matches username console-chat-id)
       (h/matches username wallet-chat-id)
       (str/includes? username chat-consts/command-char)])))

(defn correct-email? [email]
  (let [pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"]
    (or (str/blank? email)
        (and (string? email) (re-matches pattern email)))))

(s/def ::name correct-name?)
(s/def ::email correct-email?)

(s/def ::profile (s/keys :req-un [::name]))
