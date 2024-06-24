(ns status-im.common.validation.keypair
  (:require [clojure.string :as string]
            [status-im.common.validation.general :as validators]
            [status-im.constants :as constants]
            utils.emojilib
            [utils.i18n :as i18n]))

(defn keypair-too-short?
  [s]
  (< (-> s str string/trim count) constants/key-pair-name-min-length))

(defn keypair-too-long?
  [s]
  (> (-> s str string/trim count) constants/key-pair-name-max-length))

(defn validation-keypair-name
  [s existing-keypair-names]
  (cond
    (string/blank? s)                      nil
    (validators/has-emojis? s)             (i18n/label :t/key-name-error-emoji)
    (validators/has-special-characters? s) (i18n/label :t/key-name-error-special-char)
    (keypair-too-short? s)                 (i18n/label :t/key-name-error-too-short
                                                       {:count constants/key-pair-name-min-length})
    (keypair-too-long? s)                  (i18n/label :t/your-key-pair-name-is-too-long)
    (contains? existing-keypair-names s)   (i18n/label :t/key-name-error-taken)))
