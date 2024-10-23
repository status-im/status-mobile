(ns status-im.contexts.keycard.utils
  (:require [clojure.string :as string]
            [utils.address :as address]
            [utils.transforms :as transforms]))

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
   (= error "Malformed card response")
   (re-matches #".*NFCError:100.*" error)))

(defn validate-application-info
  [profile-key-uid
   {:keys [key-uid has-master-key? paired? pin-retry-counter puk-retry-counter] :as application-info}]

  (cond
    (empty? application-info)
    :keycard/error.not-keycard

    (not has-master-key?)
    :keycard/error.keycard-blank

    (and (zero? pin-retry-counter)
         (or (nil? puk-retry-counter)
             (pos? puk-retry-counter)))
    :keycard/error.keycard-frozen

    (zero? puk-retry-counter)
    :keycard/error.keycard-locked

    (not paired?)
    :keycard/error.keycard-unpaired

    (not= profile-key-uid key-uid)
    :keycard/error.keycard-wrong-profile

    :else
    nil))

(defn- error-object->map
  [^js object]
  {:code  (.-code object)
   :error (.-message object)})

(defn normalize-key-uid
  [{:keys [key-uid] :as data}]
  (if (string/blank? key-uid)
    data
    (update data :key-uid address/normalized-hex)))

(defn get-on-success
  [{:keys [on-success]}]
  #(when on-success (on-success (normalize-key-uid (transforms/js->clj %)))))

(defn get-on-failure
  [{:keys [on-failure]}]
  #(when on-failure (on-failure (error-object->map %))))

(defn wrap-handlers
  [args]
  (assoc
   args
   :on-success (get-on-success args)
   :on-failure (get-on-failure args)))
