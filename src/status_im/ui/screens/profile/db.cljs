(ns status-im.ui.screens.profile.db
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [status-im.chat.constants :as chat.constants]))

(defn correct-name? [username]
  (when-let [username (some-> username (string/trim))]
    (every? false?
            [(string/blank? username)
             (string/includes? username chat.constants/command-char)])))

(defn base64-encoded-image-path? [photo-path]
  (or (string/starts-with? photo-path "data:image/jpeg;base64,")
      (string/starts-with? photo-path "data:image/png;base64,")))

(spec/def :profile/name correct-name?)
(spec/def :profile/status (spec/nilable string?))
(spec/def :profile/photo-path (spec/nilable base64-encoded-image-path?))

(spec/def :my-profile/default-name (spec/nilable string?))
(spec/def :my-profile/editing? (spec/nilable boolean?))
(spec/def :my-profile/advanced? (spec/nilable boolean?))
(spec/def :my-profile/seed (spec/nilable map?))
(spec/def :my-profile/profile (spec/keys :opt-un [::name :profile/status :profile/photo-path
                                                  ::edit-status? ::valid-name?]))
