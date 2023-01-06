(ns status-im.multiaccounts.core
  (:require [clojure.string :as string]
            [quo.platform :as platform]
            [re-frame.core :as re-frame]
            [status-im.bottom-sheet.core :as bottom-sheet]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.native-module.core :as native-module]
            [status-im.theme.core :as theme]
            [utils.re-frame :as rf]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.utils :as utils]
            [status-im2.common.theme.core :as utils.theme]
            [taoensso.timbre :as log]))

;; validate that the given mnemonic was generated from Status Dictionary
(re-frame/reg-fx
 ::validate-mnemonic
 (fn [[passphrase success-callback error-callback]]
   (native-module/validate-mnemonic passphrase success-callback error-callback)))

(defn contact-names
  "Returns map of all existing names for contact"
  [{:keys [name
           display-name
           preferred-name
           alias
           public-key
           ens-verified
           nickname]}]
  (let [ens-name (or preferred-name
                     name)]
    (cond-> {:nickname         nickname
             :display-name     display-name
             :three-words-name (or alias (gfycat/generate-gfy public-key))}
      ;; Preferred name is our own otherwise we make sure it's verified
      (or preferred-name (and ens-verified name))
      (assoc :ens-name (str "@" (or (stateofus/username ens-name) ens-name))))))

;; NOTE: this does a bit of unnecessary work, we could short-circuit the work
;; once the first two are found, i.e don't calculate short key if 2 are already
;; available
(defn contact-two-names
  "Returns vector of two names in next order nickname, ens name, display-name, three word name, public key"
  [{:keys [names public-key] :as contact} public-key?]
  (let [{:keys [nickname
                ens-name
                display-name
                three-words-name]}
        (or names (contact-names contact))
        short-public-key (when public-key? (utils/get-shortened-address public-key))]
    (->> [nickname ens-name display-name three-words-name short-public-key]
         (remove string/blank?)
         (take 2))))

(defn contact-with-names
  "Returns contact with :names map "
  [contact]
  (assoc contact :names (contact-names contact)))

(defn displayed-name
  "Use preferred name, name or alias in that order"
  [{:keys [name preferred-name alias public-key ens-verified]}]
  (let [ens-name (or preferred-name
                     name)]
    ;; Preferred name is our own otherwise we make sure it's verified
    (if (or preferred-name (and ens-verified name))
      (let [username (stateofus/username ens-name)]
        (str "@" (or username ens-name)))
      (or alias (gfycat/generate-gfy public-key)))))

(defn contact-by-identity
  [contacts identity]
  (or (get contacts identity)
      (contact-with-names {:public-key identity})))

(defn contact-two-names-by-identity
  [contact current-multiaccount identity]
  (let [me? (= (:public-key current-multiaccount) identity)]
    (if me?
      [(or (:preferred-name current-multiaccount)
           (gfycat/generate-gfy identity))]
      (contact-two-names contact false))))

(def photo-quality-thumbnail :thumbnail)
(def photo-quality-large :large)

(defn displayed-photo
  "If a photo, a image or an images array is set use it, otherwise fallback on identicon or generate"
  [{:keys [images identicon public-key]}]
  (cond
    (pos? (count images))
    (:uri (or (photo-quality-thumbnail images)
              (photo-quality-large images)
              (first images)))

    (not (string/blank? identicon))
    identicon

    :else
    (identicon/identicon public-key)))

(re-frame/reg-fx
 ::webview-debug-changed
 (fn [value]
   (when platform/android?
     (native-module/toggle-webview-debug value))))

(re-frame/reg-fx
 ::blank-preview-flag-changed
 (fn [flag]
   (native-module/set-blank-preview-flag flag)))

(rf/defn confirm-wallet-set-up
  {:events [:multiaccounts.ui/wallet-set-up-confirmed]}
  [cofx]
  (multiaccounts.update/multiaccount-update cofx
                                            :wallet-set-up-passed?
                                            true
                                            {}))

(rf/defn confirm-home-tooltip
  {:events [:multiaccounts.ui/hide-home-tooltip]}
  [cofx]
  (multiaccounts.update/multiaccount-update cofx
                                            :hide-home-tooltip?
                                            true
                                            {}))

(rf/defn switch-webview-debug
  {:events [:multiaccounts.ui/switch-webview-debug]}
  [{:keys [db] :as cofx} value]
  (rf/merge cofx
            {::webview-debug-changed value}
            (multiaccounts.update/multiaccount-update
             :webview-debug
             (boolean value)
             {})))

(rf/defn switch-preview-privacy-mode
  {:events [:multiaccounts.ui/preview-privacy-mode-switched]}
  [{:keys [db] :as cofx} private?]
  (rf/merge cofx
            {::blank-preview-flag-changed private?}
            (multiaccounts.update/multiaccount-update
             :preview-privacy?
             (boolean private?)
             {})))

(rf/defn switch-webview-permission-requests?
  {:events [:multiaccounts.ui/webview-permission-requests-switched]}
  [cofx enabled?]
  (multiaccounts.update/multiaccount-update
   cofx
   :webview-allow-permission-requests?
   (boolean enabled?)
   {}))

(rf/defn switch-default-sync-period
  {:events [:multiaccounts.ui/default-sync-period-switched]}
  [cofx value]
  (multiaccounts.update/multiaccount-update
   cofx
   :default-sync-period
   value
   {}))

(rf/defn switch-preview-privacy-mode-flag
  [{:keys [db]}]
  (let [private? (get-in db [:multiaccount :preview-privacy?])]
    {::blank-preview-flag-changed private?}))

(re-frame/reg-fx
 :multiaccounts.ui/switch-theme
 (fn [theme-id]
   (let [theme (if (or (= 2 theme-id) (and (= 0 theme-id) (utils.theme/dark-mode?)))
                 :dark
                 :light)]
     (theme/change-theme theme))))

(rf/defn switch-appearance
  {:events [:multiaccounts.ui/appearance-switched]}
  [cofx theme]
  (rf/merge cofx
            {:multiaccounts.ui/switch-theme theme}
            (multiaccounts.update/multiaccount-update :appearance theme {})))

(rf/defn switch-profile-picture-show-to
  {:events [:multiaccounts.ui/profile-picture-show-to-switched]}
  [cofx id]
  (rf/merge cofx
            {:json-rpc/call [{:method     "wakuext_changeIdentityImageShowTo"
                              :params     [id]
                              :on-success #(log/debug "picture settings changed successfully")}]}
            (multiaccounts.update/optimistic :profile-pictures-show-to id)))

(rf/defn switch-appearance-profile
  {:events [:multiaccounts.ui/appearance-profile-switched]}
  [cofx id]
  (multiaccounts.update/multiaccount-update cofx :profile-pictures-visibility id {}))

(defn clean-path
  [path]
  (if path
    (string/replace-first path #"file://" "")
    (log/warn "[native-module] Empty path was provided")))

(rf/defn save-profile-picture
  {:events [::save-profile-picture]}
  [cofx path ax ay bx by]
  (let [key-uid (get-in cofx [:db :multiaccount :key-uid])]
    (rf/merge cofx
              {:json-rpc/call [{:method     "multiaccounts_storeIdentityImage"
                                :params     [key-uid (clean-path path) ax ay bx by]
                                ;; NOTE: In case of an error we can show a toast error
                                :on-success #(re-frame/dispatch [::update-local-picture %])}]}
              (bottom-sheet/hide-bottom-sheet))))

(rf/defn save-profile-picture-from-url
  {:events [::save-profile-picture-from-url]}
  [cofx url]
  (let [key-uid (get-in cofx [:db :multiaccount :key-uid])]
    (rf/merge cofx
              {:json-rpc/call [{:method     "multiaccounts_storeIdentityImageFromURL"
                                :params     [key-uid url]
                                :on-error   #(log/error "::save-profile-picture-from-url error" %)
                                :on-success #(re-frame/dispatch [::update-local-picture %])}]}
              (bottom-sheet/hide-bottom-sheet))))

(comment
  (re-frame/dispatch
   [::save-profile-picture-from-url
    "https://lh3.googleusercontent.com/XuKjNm3HydsaxbPkbpGs9YyCKhn5QQk5oDC8XF2jzmPyYXeZofxFtfUDZuQ3EVmacS_BlBKzbX2ypm37YNX3n1fDJA3WndeFcPsp7Z0=w600"]))

(rf/defn delete-profile-picture
  {:events [::delete-profile-picture]}
  [cofx name]
  (let [key-uid (get-in cofx [:db :multiaccount :key-uid])]
    (rf/merge cofx
              {:json-rpc/call [{:method     "multiaccounts_deleteIdentityImage"
                                :params     [key-uid]
                                ;; NOTE: In case of an error we could fallback to previous image in UI
                                ;; with a toast error
                                :on-success #(log/info "[multiaccount] Delete profile image" %)}]}
              (multiaccounts.update/optimistic :images nil)
              (bottom-sheet/hide-bottom-sheet))))

(rf/defn get-profile-picture
  [cofx]
  (let [key-uid (get-in cofx [:db :multiaccount :key-uid])]
    {:json-rpc/call [{:method     "multiaccounts_getIdentityImages"
                      :params     [key-uid]
                      :on-success #(re-frame/dispatch [::update-local-picture %])}]}))

(rf/defn store-profile-picture
  {:events [::update-local-picture]}
  [cofx pics]
  (multiaccounts.update/optimistic cofx :images pics))

(comment
  ;; Test seed for Dim Venerated Yaffle, it's not here by mistake, this is just a test account
  (native-module/validate-mnemonic
   "rocket mixed rebel affair umbrella legal resemble scene virus park deposit cargo"
   prn
   (fn [error-message]
     (log/debug "error while status/validate-mnemonic"
                error-message))
  ))
