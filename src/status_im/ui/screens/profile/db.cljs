(ns status-im.ui.screens.profile.db
  (:require-macros [status-im.utils.db :refer [allowed-keys]])
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [status-im.chat.constants :as chat.constants]
            [status-im.constants :as constants]
            [status-im.utils.homoglyph :as homoglyph]))

(def account-profile-keys [:name :photo-path :status])

(defn correct-name? [username]
  (when-let [username (some-> username (string/trim))]
    (every? false?
            [(string/blank? username)
             (homoglyph/matches username constants/console-chat-id)
             (string/includes? username chat.constants/command-char)])))

(defn correct-email? [email]
  (let [pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?"]
    (or (string/blank? email)
        (and (string? email) (re-matches pattern email)))))

(defn base64-encoded-image-path? [photo-path]
  (or (string/starts-with? photo-path "data:image/jpeg;base64,")
      (string/starts-with? photo-path "data:image/png;base64,")))

(spec/def :profile/name correct-name?)
(spec/def :profile/status (spec/nilable string?))
(spec/def :profile/photo-path (spec/nilable base64-encoded-image-path?))

;; EDIT PROFILE
(spec/def :my-profile/default-name string?)
(spec/def :my-profile/profile (spec/keys :opt-un [::name :profile/status :profile/photo-path
                                                  ::edit-status? ::valid-name?]))
(spec/def :my-profile/drawer (spec/keys :opt-un [::name :profile/status
                                                 ::edit-status? ::valid-name?]))
