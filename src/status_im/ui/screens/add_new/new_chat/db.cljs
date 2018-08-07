(ns status-im.ui.screens.add-new.new-chat.db
  (:require [status-im.utils.hex :as hex]
            [status-im.i18n :as i18n]
            [cljs.spec.alpha :as spec]
            [clojure.string :as string]))

(defn validate-pub-key [whisper-identity {:keys [address public-key]}]
  (cond
    (string/blank? whisper-identity)
    (i18n/label :t/use-valid-contact-code)
    (#{(hex/normalize-hex address) (hex/normalize-hex public-key)}
     (hex/normalize-hex whisper-identity))
    (i18n/label :t/can-not-add-yourself)

    (not (spec/valid? :global/public-key whisper-identity))
    (i18n/label :t/use-valid-contact-code)))
