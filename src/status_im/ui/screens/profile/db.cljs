(ns status-im.ui.screens.profile.db
  (:require [clojure.string :as string]
            [status-im.chat.constants :as chat.constants]
            [cljs.spec.alpha :as spec]))

(defn correct-name? [username]
  (when-let [username (some-> username (string/trim))]
    (every? false?
            [(string/blank? username)
             (string/includes? username chat.constants/command-char)])))

(defn base64-png? [photo-path]
  (string/starts-with? photo-path "data:image/png;base64,"))

(defn base64-jpeg? [photo-path]
  (string/starts-with? photo-path "data:image/jpeg;base64,"))

(defn base64-encoded-image-path? [photo-path]
  (or (base64-png? photo-path)
      (base64-jpeg? photo-path)))

(spec/def :profile/name correct-name?)