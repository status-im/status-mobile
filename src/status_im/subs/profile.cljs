(ns status-im.subs.profile
  (:require
    [cljs.spec.alpha :as spec]
    [clojure.string :as string]
    [legacy.status-im.fleet.core :as fleet]
    [legacy.status-im.multiaccounts.db :as multiaccounts.db]
    [legacy.status-im.utils.currency :as currency]
    [quo.theme :as theme]
    [re-frame.core :as re-frame]
    [status-im.common.pixel-ratio :as pixel-ratio]
    [status-im.constants :as constants]
    [status-im.contexts.profile.utils :as profile.utils]
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
 :profile/login-profiles-picture
 :<- [:profile/profiles-overview]
 :<- [:mediaserver/port]
 :<- [:initials-avatar-font-file]
 (fn [[profiles port font-file] [_ target-key-uid]]
   (let [{:keys [images ens-name? customization-color] :as profile} (get profiles target-key-uid)
         image-name                                                 (-> images first :type)
         override-ring?                                             (when ens-name? false)]
     (when profile
       {:fn
        (if image-name
          (image-server/get-account-image-uri-fn {:port           port
                                                  :ratio          pixel-ratio/ratio
                                                  :image-name     image-name
                                                  :key-uid        target-key-uid
                                                  :theme          (theme/get-theme)
                                                  :override-ring? override-ring?})
          (image-server/get-initials-avatar-uri-fn
           {:port                port
            :ratio               pixel-ratio/ratio
            :key-uid             target-key-uid
            :theme               (theme/get-theme)
            :uppercase-ratio     (:uppercase-ratio constants/initials-avatar-font-conf)
            :customization-color customization-color
            :override-ring?      override-ring?
            :font-file           font-file}))}))))

;; DEPRECATED
;; use `:profile/public-key` instead
(re-frame/reg-sub
 :multiaccount/public-key
 :<- [:profile/profile]
 (fn [{:keys [public-key]}]
   public-key))

(re-frame/reg-sub
 :profile/public-key
 :<- [:profile/profile]
 (fn [{:keys [public-key]}]
   public-key))

(re-frame/reg-sub
 :profile/webview-debug
 :<- [:profile/profile]
 (fn [{:keys [webview-debug]}]
   webview-debug))

(re-frame/reg-sub
 :profile/light-client-enabled?
 :<- [:profile/profile]
 (fn [profile]
   (get-in profile [:wakuv2-config :LightClient])))

(re-frame/reg-sub
 :profile/test-networks-enabled?
 :<- [:profile/profile]
 (fn [profile]
   (:test-networks-enabled? profile)))

(re-frame/reg-sub
 :profile/is-goerli-enabled?
 :<- [:profile/profile]
 (fn [profile]
   (:is-goerli-enabled? profile)))

(re-frame/reg-sub
 :profile/peer-syncing-enabled?
 :<- [:profile/profile]
 (fn [profile]
   (:peer-syncing-enabled? profile)))

(re-frame/reg-sub
 :profile/compressed-key
 :<- [:profile/profile]
 (fn [{:keys [compressed-key]}]
   compressed-key))

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
 :profile/image
 :<- [:profile/profile-with-image]
 (fn [profile]
   (profile.utils/photo profile)))

(re-frame/reg-sub
 :multiaccount/default-account
 :<- [:wallet/accounts]
 (fn [accounts]
   (first accounts)))

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
 :account-by-address
 :<- [:profile/wallet-accounts]
 (fn [accounts [_ address]]
   (when (string? address)
     (some #(when (= (string/lower-case (:address %))
                     (string/lower-case address))
              %)
           accounts))))

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
  (let [{:keys [key-uid ens-name? images
                customization-color]} profile
        ens-name?                     (or ens-name? (seq ens-names))
        theme                         (theme/get-theme)
        avatar-opts                   (assoc avatar-opts :override-ring? (when ens-name? false))
        images-with-uri               (mapv (fn [{key-uid :keyUid image-name :type :as image}]
                                              (let [uri-fn (image-server/get-account-image-uri-fn
                                                            (merge
                                                             {:port       port
                                                              :ratio      pixel-ratio/ratio
                                                              :image-name image-name
                                                              :key-uid    key-uid
                                                              :theme      theme}
                                                             avatar-opts))]
                                                (assoc image :fn uri-fn)))
                                            images)
        new-images                    (if (seq images-with-uri)
                                        images-with-uri
                                        [{:fn (image-server/get-initials-avatar-uri-fn
                                               (merge {:port port
                                                       :ratio pixel-ratio/ratio
                                                       :uppercase-ratio
                                                       (:uppercase-ratio
                                                        constants/initials-avatar-font-conf)
                                                       :key-uid key-uid
                                                       :customization-color customization-color
                                                       :theme theme
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
 :profile/image
 :<- [:profile/profile-with-image]
 (fn [profile]
   (profile.utils/photo profile)))

(re-frame/reg-sub
 :profile/login-profile
 :<- [:profile/login]
 :<- [:profile/profiles-overview]
 (fn [[{:keys [key-uid]} profiles]]
   (get profiles key-uid)))

(re-frame/reg-sub
 :profile/login-processing
 :<- [:profile/login]
 (fn [{:keys [processing]}]
   processing))

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
