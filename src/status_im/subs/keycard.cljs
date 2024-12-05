(ns status-im.subs.keycard
  (:require [utils.re-frame :as rf]))

(rf/reg-sub
 :keycard/keycard-profile?
 (fn [db]
   (not (nil? (get-in db [:profile/profile :keycard-pairing])))))

(rf/reg-sub
 :keycard/keycard-profile
 :<- [:profile/name]
 :<- [:profile/image]
 :<- [:profile/customization-color]
 (fn [[profile-name profile-image customization-color]]
   {:profile-name        profile-name
    :profile-image       profile-image
    :customization-color customization-color}))

(defn profile-keypair-keycards?
  [{:keys [type keycards]}]
  (and (= :profile type) keycards))

(rf/reg-sub
 :keycard/keypairs-keycards
 :<- [:wallet/keypairs-list]
 (fn [keypairs]
   (transduce (comp (filter profile-keypair-keycards?)
                    (mapcat :keycards))
              conj
              keypairs)))

(rf/reg-sub
 :keycard/registered-keycards
 :<- [:keycard/keycard-profile]
 :<- [:keycard/keypairs-keycards]
 (fn [[keycard-profile keycards]]
   (map (fn [keycard]
          (assoc keycard
                 :profile-name        (:profile-name keycard-profile)
                 :profile-image       (:profile-image keycard-profile)
                 :customization-color (:customization-color keycard-profile)))
        keycards)))

(rf/reg-sub
 :keycard/nfc-enabled?
 :<- [:keycard]
 (fn [keycard]
   (:nfc-enabled? keycard)))

(rf/reg-sub
 :keycard/connected?
 :<- [:keycard]
 (fn [keycard]
   (:card-connected? keycard)))

(rf/reg-sub
 :keycard/pin
 :<- [:keycard]
 (fn [keycard]
   (:pin keycard)))

(rf/reg-sub
 :keycard/pin-retry-counter
 :<- [:keycard]
 (fn [keycard]
   (get-in keycard [:application-info :pin-retry-counter])))

(rf/reg-sub
 :keycard/connection-sheet-opts
 :<- [:keycard]
 (fn [keycard]
   (:connection-sheet-opts keycard)))

(rf/reg-sub
 :keycard/application-info-error
 :<- [:keycard]
 (fn [keycard]
   (:application-info-error keycard)))

(rf/reg-sub
 :keycard/initialized?
 :<- [:keycard]
 (fn [keycard]
   (get-in keycard [:application-info :initialized?])))
