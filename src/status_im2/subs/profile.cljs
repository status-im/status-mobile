(ns status-im2.subs.profile
  (:require
    [cljs.spec.alpha :as spec]
    [clojure.string :as string]
    [legacy.status-im.fleet.core :as fleet]
    [legacy.status-im.multiaccounts.db :as multiaccounts.db]
    [legacy.status-im.utils.currency :as currency]
    [legacy.status-im.wallet.utils :as wallet.utils]
    [quo.theme :as theme]
    [re-frame.core :as re-frame]
    [status-im2.constants :as constants]
    [utils.address :as address]
    [utils.image-server :as image-server]
    [utils.security.core :as security]))

(re-frame/reg-sub
 :profile/customization-color
 :<- [:profile/profile]
 (fn [{:keys [customization-color]}]
   (or customization-color constants/profile-default-color)))

(re-frame/reg-sub
 :profile/currency
 :<- [:profile/profile]
 (fn [{:keys [currency]}]
   (or currency constants/profile-default-currency)))

(re-frame/reg-sub
 :profile/currency-symbol
 :<- [:profile/currency]
 (fn [currency-id]
   (-> (get currency/currencies currency-id)
       :symbol)))

(re-frame/reg-sub
 :profile/onboarding-placeholder-avatar
 :<- [:mediaserver/port]
 :<- [:initials-avatar-font-file]
 (fn [[port font-file] [_ profile-pic]]
   {:fn
    (if profile-pic
      (image-server/get-account-image-uri-fn {:port           port
                                              :image-name     profile-pic
                                              :override-ring? false
                                              :theme          (theme/get-theme)})
      (image-server/get-initials-avatar-uri-fn {:port           port
                                                :theme          (theme/get-theme)
                                                :override-ring? false
                                                :font-file      font-file}))}))

(re-frame/reg-sub
 :profile/login-profiles-picture
 :<- [:profile/profiles-overview]
 :<- [:mediaserver/port]
 :<- [:initials-avatar-font-file]
 (fn [[profiles port font-file] [_ target-key-uid]]
   (let [{:keys [images ens-name?] :as profile} (get profiles target-key-uid)
         image-name                             (-> images first :type)
         override-ring?                         (when ens-name? false)]
     (when profile
       {:fn
        (if image-name
          (image-server/get-account-image-uri-fn {:port           port
                                                  :image-name     image-name
                                                  :key-uid        target-key-uid
                                                  :theme          (theme/get-theme)
                                                  :override-ring? override-ring?})
          (image-server/get-initials-avatar-uri-fn {:port           port
                                                    :key-uid        target-key-uid
                                                    :theme          (theme/get-theme)
                                                    :override-ring? override-ring?
                                                    :font-file      font-file}))}))))

(re-frame/reg-sub
 :multiaccount/public-key
 :<- [:profile/profile]
 (fn [{:keys [public-key]}]
   public-key))

(re-frame/reg-sub
 :multiaccount/contact
 :<- [:profile/profile]
 (fn [current-account]
   (select-keys current-account [:name :preferred-name :public-key :image :images])))

(re-frame/reg-sub
 :multiaccount/preferred-name
 :<- [:profile/profile]
 (fn [{:keys [preferred-name]}]
   preferred-name))

(re-frame/reg-sub
 :multiaccount/default-account
 :<- [:profile/wallet-accounts]
 (fn [accounts]
   (wallet.utils/get-default-account accounts)))

(re-frame/reg-sub
 :multiaccount/visible-accounts
 :<- [:profile/wallet-accounts]
 (fn [accounts]
   (remove :hidden accounts)))

(re-frame/reg-sub
 :sign-in-enabled?
 :<- [:profile/login]
 (fn [{:keys [password]}]
   (spec/valid? ::multiaccounts.db/password
                (security/safe-unmask-data password))))

(re-frame/reg-sub
 :fleets/current-fleet
 :<- [:profile/profile]
 (fn [multiaccount]
   (fleet/current-fleet-sub multiaccount)))

(re-frame/reg-sub
 :opensea-enabled?
 :<- [:profile/profile]
 (fn [{:keys [opensea-enabled?]}]
   (boolean opensea-enabled?)))

(re-frame/reg-sub
 :log-level/current-log-level
 :<- [:profile/profile]
 (fn [multiaccount]
   (get multiaccount :log-level)))

(re-frame/reg-sub
 :waku/bloom-filter-mode
 :<- [:profile/profile]
 (fn [multiaccount]
   (boolean (get multiaccount :waku-bloom-filter-mode))))

(re-frame/reg-sub
 :waku/v2-flag
 :<- [:fleets/current-fleet]
 (fn [fleet]
   (string/starts-with? (name fleet) "wakuv2")))

(re-frame/reg-sub
 :dapps-address
 :<- [:profile/profile]
 (fn [acc]
   (get acc :dapps-address)))

(re-frame/reg-sub
 :dapps-account
 :<- [:profile/wallet-accounts]
 :<- [:dapps-address]
 (fn [[accounts address]]
   (some #(when (= (:address %) address) %) accounts)))

(re-frame/reg-sub
 :multiaccount/current-account
 :<- [:profile/wallet-accounts]
 :<- [:get-screen-params :wallet-account]
 (fn [[accounts acc]]
   (some #(when (= (string/lower-case (:address %))
                   (string/lower-case (:address acc)))
            %)
         accounts)))

(re-frame/reg-sub
 :account-by-address
 :<- [:profile/wallet-accounts]
 (fn [accounts [_ address]]
   (when (string? address)
     (some #(when (= (string/lower-case (:address %))
                     (string/lower-case address))
              %)
           accounts))))

;; NOTE: this subscription only works on login
(re-frame/reg-sub
 :multiaccounts.login/keycard-account?
 :<- [:profile/profiles-overview]
 :<- [:profile/login]
 (fn [[multiaccounts {:keys [key-uid]}]]
   (get-in multiaccounts [key-uid :keycard-pairing])))

(re-frame/reg-sub
 :multiaccounts/keycard-account?
 :<- [:profile/profile]
 (fn [multiaccount]
   (:keycard-pairing multiaccount)))

(re-frame/reg-sub
 :accounts-without-watch-only
 :<- [:profile/wallet-accounts]
 (fn [accounts]
   (filter #(not= (:type %) :watch) accounts)))

(re-frame/reg-sub
 :visible-accounts-without-watch-only
 :<- [:profile/wallet-accounts]
 (fn [accounts]
   (remove :hidden (filter #(not= (:type %) :watch) accounts))))

(defn filter-recipient-accounts
  [search-filter {:keys [name]}]
  (string/includes? (string/lower-case (str name)) search-filter))

(re-frame/reg-sub
 :accounts-for-recipient
 :<- [:multiaccount/visible-accounts]
 :<- [:wallet-legacy/prepare-transaction]
 :<- [:wallet-legacy/search-recipient-filter]
 (fn [[accounts {:keys [from]} search-filter]]
   (let [accounts (remove #(= (:address %) (:address from)) accounts)]
     (if (string/blank? search-filter)
       accounts
       (filter (partial filter-recipient-accounts
                        (string/lower-case search-filter))
               accounts)))))

(re-frame/reg-sub
 :add-account-disabled?
 :<- [:profile/wallet-accounts]
 :<- [:add-account]
 (fn [[accounts {:keys [address type account seed private-key]}]]
   (or (string/blank? (:name account))
       (case type
         :generate
         false
         :watch
         (or (not (address/address? address))
             (some #(when (= (:address %) address) %) accounts))
         :key
         (string/blank? (security/safe-unmask-data private-key))
         :seed
         (string/blank? (security/safe-unmask-data seed))
         false))))

(re-frame/reg-sub
 :multiaccount/current-user-visibility-status
 :<- [:profile/profile]
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
 :<- [:profile/profile]
 (fn [multiaccount]
   (pos? (count (get multiaccount :images)))))

(defn- replace-multiaccount-image-uri
  [profile ens-names port font-file avatar-opts]
  (let [{:keys [key-uid ens-name? images]} profile
        ens-name?                          (or ens-name? (seq ens-names))
        theme                              (theme/get-theme)
        avatar-opts                        (assoc avatar-opts :override-ring? (when ens-name? false))
        images-with-uri                    (mapv (fn [{key-uid :keyUid image-name :type :as image}]
                                                   (let [uri-fn (image-server/get-account-image-uri-fn
                                                                 (merge {:port       port
                                                                         :image-name image-name
                                                                         :key-uid    key-uid
                                                                         :theme      theme}
                                                                        avatar-opts))]
                                                     (assoc image :fn uri-fn)))
                                                 images)
        new-images                         (if (seq images-with-uri)
                                             images-with-uri
                                             [{:fn (image-server/get-initials-avatar-uri-fn
                                                    (merge {:port      port
                                                            :key-uid   key-uid
                                                            :theme     theme
                                                            :font-file font-file}
                                                           avatar-opts))}])]
    (assoc profile :images new-images)))

(re-frame/reg-sub
 :profile/profile-with-image
 :<- [:profile/profile]
 :<- [:ens/current-names]
 :<- [:mediaserver/port]
 :<- [:initials-avatar-font-file]
 (fn [[profile ens-names port font-file] [_ avatar-opts]]
   (replace-multiaccount-image-uri profile ens-names port font-file avatar-opts)))

(re-frame/reg-sub
 :profile/login-profile
 :<- [:profile/login]
 :<- [:profile/profiles-overview]
 (fn [[{:keys [key-uid]} profiles]]
   (get profiles key-uid)))

;; LINK PREVIEW
;; ========================================================================================================

(re-frame/reg-sub
 :link-preview/cache
 :<- [:profile/profile]
 (fn [multiaccount [_ link]]
   (get-in multiaccount [:link-previews-cache link])))

(re-frame/reg-sub
 :link-preview/enabled-sites
 :<- [:profile/profile]
 (fn [multiaccount]
   (get multiaccount :link-previews-enabled-sites)))

(re-frame/reg-sub
 :link-preview/link-preview-request-enabled
 :<- [:profile/profile]
 (fn [multiaccount]
   (get multiaccount :link-preview-request-enabled)))
