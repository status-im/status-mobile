(ns status-im.utils.signing-phrase.core
  (:require [status-im.utils.signing-phrase.dictionaries.en :as en]
            [clojure.string :as string]))

; In order to reduce phishing threat for Status.im users we want to have them
; recognize 3 predefined words when they sign transactions or make other sensitive operations.
;
; Onboarding flow needs to generate the 3 words for the user and store it in the profile.
; In order to reduce phishing threat for users we want to have them recognize 3
; predefined words when they sign transactions or make other sensitive operations.
; Onboarding flow needs to generate the 3 words for the user and store it in the profile.
;
; As a user, I want to accept 3 generated words when creating my account so that I can recognize these words when signing transactions
; and thus make it hard to create a phishing page and protect myself from phishing attack.
;
; See more info at: https://github.com/status-im/status-react/issues/1585
;
; Currently only English is supported (as the default language),
; for more details see #https://github.com/status-im/status-react/issues/1679

(defn pick-words [dictionary]
  (repeatedly 3 #(rand-nth dictionary)))

(defn generate []
  (string/join " " (pick-words en/dictionary)))

