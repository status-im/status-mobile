(ns status-im.common.standard-authentication.utils
  (:require [clojure.string :as string]
            [status-im.common.keychain.events :as keychain]))

(defn keycard-profile?
  [db]
  (boolean (get-in db [:profile/profile :keycard-pairing])))

(defn profile-keypair-keycards?
  [{:keys [type keycards]}]
  (and (= :profile type) (seq keycards)))

(defn keycard-account?
  [keypairs address]
  (let [find-keycard-keypair (fn [kps] (some #(when (profile-keypair-keycards? %) %) kps))
        keypair-addresses    (fn [kp]
                               (->> (:accounts kp)
                                    (map :address)
                                    set))]
    (-> keypairs
        vals
        find-keycard-keypair
        keypair-addresses
        (contains? (string/lower-case address)))))

(defn- keypair-by-address
  [keypairs address]
  (some (fn [keypair]
          (let [keypair-addresses (->> keypair
                                       :accounts
                                       (map :address)
                                       set)]
            (when (contains? keypair-addresses address)
              keypair)))
        (vals keypairs)))

(defn payloads-for-different-keypairs?
  "Returns true if the payloads are for addresses that are derived from different keypairs"
  [keypairs payloads]
  (let [address-to-keypair  (partial keypair-by-address keypairs)
        payload-keypair-ids (->> payloads
                                 (map :address)
                                 (map address-to-keypair)
                                 (map :key-uid)
                                 set)]
    (> (count payload-keypair-ids) 1)))

(defn biometrics-enabled?
  [db]
  (= (:auth-method db) keychain/auth-method-biometric))
