(ns status-im.ui.screens.add-new.new-chat.db
  (:require [status-im.utils.hex :as hex]
            [status-im.i18n :as i18n]
            [cljs.spec.alpha :as spec]
            [clojure.string :as string]))

(defn own-whisper-identity?
  [{{:keys [public-key]} :account/account} whisper-identity]
  (= whisper-identity public-key))

(defn validate-pub-key [db whisper-identity]
  (cond
    (not (spec/valid? :global/public-key whisper-identity))
    (i18n/label :t/use-valid-contact-code)

    (own-whisper-identity? db whisper-identity)
    (i18n/label :t/can-not-add-yourself)))
