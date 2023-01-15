(ns status-im.profile.db
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [status-im.chat.constants :as chat.constants]))

(defn correct-name?
  [username]
  (when-let [username (some-> username
                              (string/trim))]
    (every? false?
            [(string/blank? username)
             (string/includes? username chat.constants/command-char)])))

(defn base64-png?
  [photo-path]
  (string/starts-with? photo-path "data:image/png;base64,"))

(defn base64-jpeg?
  [photo-path]
  (string/starts-with? photo-path "data:image/jpeg;base64,"))

(spec/def :profile/name correct-name?)