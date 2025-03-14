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
   (= error "NFCError:102")
   (= error "Malformed card response")
   (re-matches #".*NFCError:100.*" error)))

(defn validate-application-info
  [{:keys [has-master-key? paired? pin-retry-counter puk-retry-counter] :as application-info}
   instance-uid key-uid]

  (cond
    (empty? application-info)
    :keycard/error.not-keycard

    (or (and instance-uid (not= (:instance-uid application-info) instance-uid))
        (and key-uid (not= (:key-uid application-info) key-uid)))
    :keycard/error.keycard-different

    (and (zero? pin-retry-counter)
         (or (nil? puk-retry-counter)
             (pos? puk-retry-counter)))
    :keycard/error.keycard-frozen

    (zero? puk-retry-counter)
    :keycard/error.keycard-locked

    (not has-master-key?)
    nil

    (not paired?)
    :keycard/error.keycard-unpaired

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

(defn format-success-data
  [data]
  (normalize-key-uid (transforms/js->clj data)))

(defn get-on-success
  [{:keys [on-success]}]
  #(when on-success (on-success (format-success-data %))))

(defn get-on-failure
  [{:keys [on-failure]}]
  #(when on-failure (on-failure (error-object->map %))))

(defn wrap-handlers
  [args]
  (assoc
   args
   :on-success (get-on-success args)
   :on-failure (get-on-failure args)))

(defn keycard-address?
  [keypairs address]
  (let [find-keycard-keypair (fn [kps] (some #(when-not (empty? (:keycards %)) %) kps))
        keypair-addresses    (fn [kp]
                               (->> (:accounts kp)
                                    (map :address)
                                    set))]
    (when-not (nil? address)
      (-> keypairs
          vals
          find-keycard-keypair
          keypair-addresses
          (contains? (string/lower-case address))))))
