(ns status-im.common.validation.profile
  (:require [clojure.string :as string]
            [status-im.common.validation.general :as validators]
            [status-im.constants :as constants]
            utils.emojilib
            [utils.i18n :as i18n]))

;; NOTE - validation should match with Desktop
;; https://github.com/status-im/status-desktop/blob/2ba96803168461088346bf5030df750cb226df4c/ui/imports/utils/Constants.qml#L468
(def min-length 5)

;; Combine ASCII and Emoji into one regex
(def bio-chars-regex
  #"^[\u00a9\u00ae\u2000-\u3300\ud83c\ud000-\udfff\ud83d\ud000-\udfff\ud83e\ud000-\udfff\u0000-\u007F]+$")

(def common-names ["Ethereum" "Bitcoin"])

(defn has-common-names? [s] (pos? (count (filter #(string/includes? s %) common-names))))

(defn name-too-short? [s] (< (count (string/trim (str s))) min-length))

(defn name-too-long? [s] (> (count (string/trim (str s))) constants/profile-name-max-length))

(defn bio-too-long? [s] (> (count (string/trim (str s))) constants/profile-bio-max-length))

(defn bio-invalid-characters?
  [s]
  (not (re-find bio-chars-regex s)))

(defn validation-name
  [s]
  (cond
    (string/blank? s)                      nil
    (string/ends-with? s "-eth")           (i18n/label :t/ending-not-allowed {:ending "-eth"})
    (string/ends-with? s "_eth")           (i18n/label :t/ending-not-allowed {:ending "_eth"})
    (string/ends-with? s ".eth")           (i18n/label :t/ending-not-allowed {:ending ".eth"})
    (string/starts-with? s " ")            (i18n/label :t/start-with-space)
    (string/ends-with? s " ")              (i18n/label :t/ends-with-space)
    (has-common-names? s)                  (i18n/label :t/are-not-allowed
                                                       {:check (i18n/label :t/common-names)})
    (validators/has-emojis? s)             (i18n/label :t/are-not-allowed
                                                       {:check (i18n/label :t/emojis)})
    (validators/has-special-characters? s) (i18n/label :t/are-not-allowed
                                                       {:check (i18n/label :t/special-characters)})
    (name-too-short? s)                    (i18n/label :t/minimum-characters {:min-chars min-length})
    (name-too-long? s)                     (i18n/label :t/profile-name-is-too-long)))

(defn validation-bio
  [s]
  (cond
    (string/blank? s)           nil
    (bio-invalid-characters? s) (i18n/label :t/invalid-characters-bio)
    (bio-too-long? s)           (i18n/label :t/bio-is-too-long)))

(defn validation-nickname
  [s]
  (cond
    (string/blank? s)                      nil
    (validators/has-emojis? s)             (i18n/label :t/are-not-allowed
                                                       {:check (i18n/label :t/emojis)})
    (validators/has-special-characters? s) (i18n/label :t/are-not-allowed
                                                       {:check (i18n/label :t/special-characters)})
    (name-too-short? s)                    (i18n/label :t/minimum-characters {:min-chars min-length})
    (name-too-long? s)                     (i18n/label :t/nickname-is-too-long)))
