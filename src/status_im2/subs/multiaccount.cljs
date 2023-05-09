(ns status-im2.subs.multiaccount
  (:require [cljs.spec.alpha :as spec]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.core :as ethereum]
            [status-im.fleet.core :as fleet]
            [status-im.multiaccounts.db :as multiaccounts.db]
            [utils.image-server :as image-server]
            [utils.security.core :as security]
            [quo2.theme :as theme]))

(re-frame/reg-sub
 :multiaccount/public-key
 :<- [:multiaccount]
 (fn [{:keys [public-key]}]
   public-key))

(re-frame/reg-sub
 :multiaccount/contact
 :<- [:multiaccount]
 (fn [current-account]
   (select-keys current-account [:name :preferred-name :public-key :identicon :image :images])))

(re-frame/reg-sub
 :multiaccount/preferred-name
 :<- [:multiaccount]
 (fn [{:keys [preferred-name]}]
   preferred-name))

(re-frame/reg-sub
 :multiaccount/default-account
 :<- [:multiaccount/accounts]
 (fn [accounts]
   (ethereum/get-default-account accounts)))

(re-frame/reg-sub
 :multiaccount/visible-accounts
 :<- [:multiaccount/accounts]
 (fn [accounts]
   (remove :hidden accounts)))

(re-frame/reg-sub
 :sign-in-enabled?
 :<- [:multiaccounts/login]
 (fn [{:keys [password]}]
   (spec/valid? ::multiaccounts.db/password
                (security/safe-unmask-data password))))

(re-frame/reg-sub
 :fleets/current-fleet
 :<- [:multiaccount]
 (fn [multiaccount]
   (fleet/current-fleet-sub multiaccount)))

(re-frame/reg-sub
 :opensea-enabled?
 :<- [:multiaccount]
 (fn [{:keys [opensea-enabled?]}]
   (boolean opensea-enabled?)))

(re-frame/reg-sub
 :log-level/current-log-level
 :<- [:multiaccount]
 (fn [multiaccount]
   (get multiaccount :log-level)))

(re-frame/reg-sub
 :waku/bloom-filter-mode
 :<- [:multiaccount]
 (fn [multiaccount]
   (boolean (get multiaccount :waku-bloom-filter-mode))))

(re-frame/reg-sub
 :waku/v2-flag
 :<- [:fleets/current-fleet]
 (fn [fleet]
   (string/starts-with? (name fleet) "wakuv2")))

(re-frame/reg-sub
 :dapps-address
 :<- [:multiaccount]
 (fn [acc]
   (get acc :dapps-address)))

(re-frame/reg-sub
 :dapps-account
 :<- [:multiaccount/accounts]
 :<- [:dapps-address]
 (fn [[accounts address]]
   (some #(when (= (:address %) address) %) accounts)))

(re-frame/reg-sub
 :multiaccount/current-account
 :<- [:multiaccount/accounts]
 :<- [:get-screen-params :wallet-account]
 (fn [[accounts acc]]
   (some #(when (= (string/lower-case (:address %))
                   (string/lower-case (:address acc)))
            %)
         accounts)))

(re-frame/reg-sub
 :account-by-address
 :<- [:multiaccount/accounts]
 (fn [accounts [_ address]]
   (when (string? address)
     (some #(when (= (string/lower-case (:address %))
                     (string/lower-case address))
              %)
           accounts))))

(re-frame/reg-sub
 :multiple-multiaccounts?
 :<- [:multiaccounts/multiaccounts]
 (fn [multiaccounts]
   (> (count multiaccounts) 1)))

;; NOTE: this subscription only works on login
(re-frame/reg-sub
 :multiaccounts.login/keycard-account?
 :<- [:multiaccounts/multiaccounts]
 :<- [:multiaccounts/login]
 (fn [[multiaccounts {:keys [key-uid]}]]
   (get-in multiaccounts [key-uid :keycard-pairing])))

(re-frame/reg-sub
 :multiaccounts/keycard-account?
 :<- [:multiaccount]
 (fn [multiaccount]
   (:keycard-pairing multiaccount)))

(re-frame/reg-sub
 :accounts-without-watch-only
 :<- [:multiaccount/accounts]
 (fn [accounts]
   (filter #(not= (:type %) :watch) accounts)))

(re-frame/reg-sub
 :visible-accounts-without-watch-only
 :<- [:multiaccount/accounts]
 (fn [accounts]
   (remove :hidden (filter #(not= (:type %) :watch) accounts))))

(defn filter-recipient-accounts
  [search-filter {:keys [name]}]
  (string/includes? (string/lower-case (str name)) search-filter))

(re-frame/reg-sub
 :accounts-for-recipient
 :<- [:multiaccount/visible-accounts]
 :<- [:wallet/prepare-transaction]
 :<- [:search/recipient-filter]
 (fn [[accounts {:keys [from]} search-filter]]
   (let [accounts (remove #(= (:address %) (:address from)) accounts)]
     (if (string/blank? search-filter)
       accounts
       (filter (partial filter-recipient-accounts
                        (string/lower-case search-filter))
               accounts)))))

(re-frame/reg-sub
 :add-account-disabled?
 :<- [:multiaccount/accounts]
 :<- [:add-account]
 (fn [[accounts {:keys [address type account seed private-key]}]]
   (or (string/blank? (:name account))
       (case type
         :generate
         false
         :watch
         (or (not (ethereum/address? address))
             (some #(when (= (:address %) address) %) accounts))
         :key
         (string/blank? (security/safe-unmask-data private-key))
         :seed
         (string/blank? (security/safe-unmask-data seed))
         false))))

(re-frame/reg-sub
 :multiaccount/current-user-visibility-status
 :<- [:multiaccount]
 (fn [{:keys [current-user-visibility-status]}]
   current-user-visibility-status))

(re-frame/reg-sub
 :multiaccount/reset-password-form-vals-and-errors
 :<- [:multiaccount/reset-password-form-vals]
 :<- [:multiaccount/reset-password-errors]
 :<- [:multiaccount/resetting-password?]
 (fn [[form-vals errors resetting?]]
   (let [{:keys [current-password new-password confirm-new-password]} form-vals]
     {:form-vals form-vals
      :errors errors
      :resetting? resetting?
      :next-enabled?
      (and (pos? (count current-password))
           (pos? (count new-password))
           (pos? (count confirm-new-password))
           (>= (count new-password) 6)
           (>= (count current-password) 6)
           (= new-password confirm-new-password))})))

(re-frame/reg-sub
 :profile/has-picture
 :<- [:multiaccount]
 (fn [multiaccount]
   (pos? (count (get multiaccount :images)))))

(defn- replace-multiaccount-image-uri
  [multiaccount port]
  (let [public-key   (:public-key multiaccount)
        theme        (theme/get-theme)
        identicon    (image-server/get-identicons-uri port public-key theme)
        multiaccount (assoc multiaccount :identicon identicon)
        images       (:images multiaccount)
        images       (reduce (fn [acc current]
                               (let [key-uid    (:keyUid current)
                                     image-name (:type current)
                                     uri        (image-server/get-account-image-uri port
                                                                                    public-key
                                                                                    image-name
                                                                                    key-uid
                                                                                    theme)]
                                 (conj acc (assoc current :uri uri))))
                             []
                             images)]
    (assoc multiaccount :images images)))

(re-frame/reg-sub
 :profile/multiaccount
 :<- [:multiaccount]
 :<- [:mediaserver/port]
 (fn [[multiaccount port]]
   (replace-multiaccount-image-uri multiaccount port)))

;; LINK PREVIEW
;; ========================================================================================================

(re-frame/reg-sub
 :link-preview/cache
 :<- [:multiaccount]
 (fn [multiaccount [_ link]]
   (get-in multiaccount [:link-previews-cache link])))

(re-frame/reg-sub
 :link-preview/enabled-sites
 :<- [:multiaccount]
 (fn [multiaccount]
   (get multiaccount :link-previews-enabled-sites)))

(re-frame/reg-sub
 :link-preview/link-preview-request-enabled
 :<- [:multiaccount]
 (fn [multiaccount]
   (get multiaccount :link-preview-request-enabled)))
