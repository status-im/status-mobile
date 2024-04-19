(ns status-im.contexts.profile.utils
  (:require [clojure.string :as string]
            [native-module.core :as native-module]
            [status-im.common.emoji-picker.utils :as emoji-picker.utils]
            [status-im.contexts.profile.config :as profile.config]
            [utils.security.core :as security]))

(defn displayed-name
  [{:keys [name display-name preferred-name alias ens-verified primary-name]}]
  ;; `preferred-name` is our own name otherwise we make sure the `name` is verified and use it
  (let [display-name   (when-not (string/blank? display-name)
                         display-name)
        preferred-name (when-not (string/blank? preferred-name)
                         preferred-name)
        ens-name       (or preferred-name
                           display-name
                           name)]
    (if (or preferred-name (and ens-verified name))
      ens-name
      (or display-name primary-name alias))))

(defn photo
  [{:keys [images]}]
  (or (:large images)
      (:thumbnail images)
      (first images)))

(defn create-profile-config
  [{:keys [display-name seed-phrase password image-path color]} restore?]
  (let [login-sha3-password (native-module/sha3 (security/safe-unmask-data password))]
    (cond-> (profile.config/create)

      true
      (merge {:displayName        display-name
              :password           login-sha3-password
              :imagePath          (profile.config/strip-file-prefix image-path)
              :customizationColor color
              :emoji              (emoji-picker.utils/random-emoji)})

      restore?
      (merge {:mnemonic    (security/safe-unmask-data seed-phrase)
              :fetchBackup true}))))
