(ns status-im.ui.screens.profile.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [status-im.chat.constants :as chat.constants]
            [status-im.constants :as constants]
            [status-im.utils.homoglyph :as homoglyph]))

(defn correct-name? [username]
  (when-let [username (some-> username (string/trim))]
    (every? false?
            [(string/blank? username)
             (homoglyph/matches username constants/console-chat-id)
             (homoglyph/matches username constants/wallet-chat-id)
             (string/includes? username chat.constants/command-char)
             (string/includes? username chat.constants/bot-char)])))

(defn correct-email? [email]
  (let [pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"]
    (or (string/blank? email)
        (and (string? email) (re-matches pattern email)))))

(defn base64-encoded-image-path? [photo-path]
  (or (string/starts-with? photo-path "data:image/jpeg;base64,")
      (string/starts-with? photo-path "data:image/png;base64,")))


(spec/def :profile/name (spec/nilable correct-name?))

(spec/def ::profile (spec/keys :req-un [:profile/name]))


(spec/def ::name (spec/or :name correct-name?
                          :empty-string string/blank?))
(spec/def ::email (spec/nilable correct-email?))
(spec/def ::edit? boolean?)
(spec/def ::status (spec/nilable string?))
(spec/def ::photo-path (spec/nilable base64-encoded-image-path?))
(spec/def ::edit-status? boolean?)


;; EDIT PROFILE
(spec/def :my-profile/edit (allowed-keys
                             :req-un [::email ::edit? ::name ::status ::photo-path]
                             :opt-un [::edit-status?]))
