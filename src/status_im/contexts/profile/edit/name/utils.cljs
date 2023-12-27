(ns status-im.contexts.profile.edit.name.utils
  (:require [clojure.string :as string]
            [utils.i18n :as i18n]))

(def min-length 5)

(def max-length 24)

(def emoji-regex
  (new
   js/RegExp
   #"(\u00a9|\u00ae|[\u2000-\u3300]|\ud83c[\ud000-\udfff]|\ud83d[\ud000-\udfff]|\ud83e[\ud000-\udfff])"
   "i"))

(defn has-emojis [s] (re-find emoji-regex s))

(def common-names ["Ethereum" "Bitcoin"])

(defn has-common-names [s] (pos? (count (filter #(string/includes? s %) common-names))))

(def status-regex (new js/RegExp #"^[a-zA-Z0-9\-_ ]+$"))

(defn has-special-characters [s] (not (re-find status-regex s)))

(defn name-too-short [s] (< (count (string/trim (str s))) min-length))

(defn name-too-long [s] (> (count (string/trim (str s))) max-length))

(defn validation-name
  [s]
  (cond
    (or (= s nil) (= s ""))      nil
    (has-special-characters s)   (i18n/label :t/are-not-allowed
                                             {:check (i18n/label :t/special-characters)})
    (string/ends-with? s "-eth") (i18n/label :t/ending-not-allowed {:ending "-eth"})
    (string/ends-with? s "_eth") (i18n/label :t/ending-not-allowed {:ending "_eth"})
    (string/ends-with? s ".eth") (i18n/label :t/ending-not-allowed {:ending ".eth"})
    (string/starts-with? s " ")  (i18n/label :t/start-with-space)
    (string/ends-with? s " ")    (i18n/label :t/ends-with-space)
    (has-common-names s)         (i18n/label :t/are-not-allowed {:check (i18n/label :t/common-names)})
    (has-emojis s)               (i18n/label :t/are-not-allowed {:check (i18n/label :t/emojis)})
    (name-too-short s)           (i18n/label :t/minimum-characters {:min-chars min-length})
    (name-too-long s)            (i18n/label :t/profile-name-is-too-long)
    :else                        nil))
