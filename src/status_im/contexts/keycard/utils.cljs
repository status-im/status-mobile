(ns status-im.contexts.keycard.utils
  (:require [taoensso.timbre :as log]))

(def pin-mismatch-error #"Unexpected error SW, 0x63C(\d+)|wrongPIN\(retryCounter: (\d+)\)")

(defn pin-retries
  [error]
  (when-let [matched-error (re-matches pin-mismatch-error error)]
    (js/parseInt (second (filter some? matched-error)))))

(defn tag-lost?
  [error]
  (or
   (= error "Tag was lost.")
   (= error "NFCError:100")
   (re-matches #".*NFCError:100.*" error)))

(defn validate-application-info
  [profile-key-uid {:keys [key-uid paired? pin-retry-counter puk-retry-counter] :as application-info}]
  (let [profile-mismatch? (or (nil? profile-key-uid) (not= profile-key-uid key-uid))]
    (log/debug "[keycard] login-with-keycard"
               "empty application info" (empty? application-info)
               "no key-uid"             (empty? key-uid)
               "profile-mismatch?"      profile-mismatch?
               "no pairing"             paired?)
    (cond
      (empty? application-info)
      :not-keycard

      (empty? (:key-uid application-info))
      :keycard-blank

      profile-mismatch?
      :keycard-wrong

      (not paired?)
      :keycard-unpaired

      (and (zero? pin-retry-counter)
           (or (nil? puk-retry-counter)
               (pos? puk-retry-counter)))
      nil

      :else
      nil)))
