(ns status-im.common.standard-authentication.utils
  (:require [clojure.string :as string]
            [status-im.common.keychain.events :as keychain]))

(defn keycard-pairing?
  [db]
  (boolean (get-in db [:profile/profile :keycard-pairing])))

(defn profile-keypair-keycards?
  [{:keys [type keycards]}]
  (and (= :profile type) (seq keycards)))

(defn keycard-address?
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

(defn profile-keypair-on-keycard?
  [keypairs]
  (->> keypairs
       vals
       (some #(when (profile-keypair-keycards? %) %))
       boolean))

(defn all-addresses-from-profile-keypair?
  [keypairs addresses]
  (let [profile-keypair   (->> keypairs
                               vals
                               (some #(when (= (:type %) :profile) %)))
        profile-addresses (->> profile-keypair
                               :accounts
                               (map :address)
                               set)]
    (every? #(contains? profile-addresses %) addresses)))

(defn biometrics-enabled?
  [db]
  (= (:auth-method db) keychain/auth-method-biometric))

(defn transaction-payload
  [transaction-for-signing]
  (let [signing-details (:signingDetails transaction-for-signing)
        address         (:address signing-details)
        messages        (:hashes signing-details)]
    (reduce (fn [acc message]
              (conj acc
                    {:address address
                     :message message}))
            []
            messages)))
