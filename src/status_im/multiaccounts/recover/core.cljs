(ns status-im.multiaccounts.recover.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.bottom-sheet.events :as bottom-sheet]
            [status-im2.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.mnemonic :as mnemonic]
            [utils.i18n :as i18n]
            [status-im.multiaccounts.core :as multiaccounts]
            [status-im.multiaccounts.create.core :as multiaccounts.create]
            [native-module.core :as native-module]
            [status-im.popover.core :as popover]
            [utils.re-frame :as rf]
            [status-im.utils.types :as types]
            [status-im2.navigation.events :as navigation]
            [taoensso.timbre :as log]
            [utils.security.core :as security]))

(defn existing-account?
  [multiaccounts key-uid]
  {:pre [(not (nil? key-uid))]}
  (contains? multiaccounts key-uid))

(defn check-phrase-warnings
  [recovery-phrase]
  (cond (string/blank? recovery-phrase) :t/required-field))

(rf/defn set-phrase
  {:events [:multiaccounts.recover/passphrase-input-changed]}
  [{:keys [db]} masked-recovery-phrase]
  (let [recovery-phrase (security/safe-unmask-data masked-recovery-phrase)]
    {:db (update db
                 :intro-wizard          assoc
                 :passphrase            (string/lower-case recovery-phrase)
                 :passphrase-error      nil
                 :next-button-disabled? (or (empty? recovery-phrase)
                                            (not (mnemonic/valid-length? recovery-phrase))))}))

(rf/defn validate-phrase-for-warnings
  [{:keys [db]}]
  (let [recovery-phrase (get-in db [:intro-wizard :passphrase])]
    {:db (update db
                 :intro-wizard     assoc
                 :passphrase-error (check-phrase-warnings recovery-phrase))}))

(rf/defn on-store-multiaccount-success
  {:events       [::store-multiaccount-success]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)
                  (re-frame/inject-cofx ::multiaccounts.create/get-signing-phrase)]}
  [{:keys [db] :as cofx} result password]
  (let [{:keys [error]} (types/json->clj result)]
    (if error
      {:utils/show-popup {:title      (i18n/label :t/multiaccount-exists-title)
                          :content    (i18n/label :t/multiaccount-exists-title)
                          :on-dismiss #(re-frame/dispatch [:pop-to-root :multiaccounts])}}
      (let [{:keys [key-uid] :as multiaccount} (get-in db [:intro-wizard :root-key])
            keycard-multiaccount?              (boolean (get-in db
                                                                [:profile/profiles-overview key-uid
                                                                 :keycard-pairing]))]
        (if keycard-multiaccount?
          ;; trying to recover multiaccount created with keycard
          {:db (-> db
                   (update :intro-wizard     assoc
                           :processing?      false
                           :passphrase-error :recover-keycard-multiaccount-not-supported)
                   (update :intro-wizard
                           dissoc
                           :passphrase-valid?))}
          (let [multiaccount (assoc multiaccount :derived (get-in db [:intro-wizard :derived]))]
            (multiaccounts.create/on-multiaccount-created cofx
                                                          multiaccount
                                                          password
                                                          {})))))))

(rf/defn store-multiaccount
  {:events [::recover-multiaccount-confirmed]}
  [{:keys [db]} password]
  (let [{:keys [root-key]}   (:intro-wizard db)
        {:keys [id key-uid]} root-key
        callback             #(re-frame/dispatch [::store-multiaccount-success % password])
        hashed-password      (ethereum/sha3 (security/safe-unmask-data password))]
    {:db                                       (assoc-in db [:intro-wizard :processing?] true)
     ::multiaccounts.create/store-multiaccount [id key-uid hashed-password callback]}))

(re-frame/reg-fx
 ::import-multiaccount
 (fn [{:keys [passphrase password success-event]}]
   (log/debug "[recover] ::import-multiaccount")
   (native-module/multiaccount-import-mnemonic
    passphrase
    password
    (fn [result]
      (let [{:keys [id] :as root-data}
            (multiaccounts.create/normalize-multiaccount-data-keys
             (types/json->clj result))]
        (native-module.core/multiaccount-derive-addresses
         id
         [constants/path-wallet-root
          constants/path-eip1581
          constants/path-whisper
          constants/path-default-wallet]
         (fn [result]
           (let [derived-data (multiaccounts.create/normalize-derived-data-keys
                               (types/json->clj result))
                 public-key   (get-in derived-data [constants/path-whisper-keyword :public-key])]
             (native-module/gfycat-identicon-async
              public-key
              (fn [name _]
                (let [derived-data-extended
                      (update derived-data constants/path-whisper-keyword assoc :name name)]
                  (re-frame/dispatch [success-event root-data derived-data-extended]))))))))))))

(rf/defn show-existing-multiaccount-alert
  [_ key-uid]
  {:utils/show-confirmation
   {:title               (i18n/label :t/multiaccount-exists-title)
    :content             (i18n/label :t/multiaccount-exists-content)
    :confirm-button-text (i18n/label :t/unlock)
    :on-accept           #(do
                            (re-frame/dispatch [:pop-to-root :multiaccounts])
                            (re-frame/dispatch
                             [:profile/profile-selected key-uid]))
    :on-cancel           #(re-frame/dispatch [:pop-to-root :multiaccounts])}})

(rf/defn on-import-multiaccount-success
  {:events [::import-multiaccount-success]}
  [{:keys [db] :as cofx} {:keys [key-uid] :as root-data} derived-data]
  (let [multiaccounts (:profile/profiles-overview db)]
    (rf/merge
     cofx
     {:db (update db
                  :intro-wizard
                  assoc
                  :root-key root-data
                  :derived derived-data
                  :step :recovery-success)}
     (when (existing-account? multiaccounts key-uid)
       (show-existing-multiaccount-alert key-uid))
     (navigation/navigate-to :recover-multiaccount-success nil))))

(rf/defn enter-phrase-pressed
  {:events [::enter-phrase-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge
   cofx
   {:db (-> db
            (assoc :intro-wizard
                   {:step                   :enter-phrase
                    :recovering?            true
                    :next-button-disabled?  true
                    :weak-password?         true
                    :encrypt-with-password? true
                    :forward-action         :multiaccounts.recover/enter-phrase-next-pressed}
                   :recovered-account? true)
            (update :keycard dissoc :flow))}
   (bottom-sheet/hide-bottom-sheet-old)
   (navigation/navigate-to :recover-multiaccount-enter-phrase nil)))

(rf/defn proceed-to-import-mnemonic
  {:events [:multiaccounts.recover/phrase-validated]}
  [{:keys [db] :as cofx} phrase-warnings]
  (let [{:keys [password passphrase]} (:intro-wizard db)]
    (if-not (string/blank? (:error (types/json->clj phrase-warnings)))
      (popover/show-popover cofx {:view :custom-seed-phrase})
      (when (mnemonic/valid-length? passphrase)
        {::import-multiaccount {:passphrase    (mnemonic/sanitize-passphrase passphrase)
                                :password      (when password
                                                 (security/safe-unmask-data password))
                                :success-event ::import-multiaccount-success}}))))

(rf/defn seed-phrase-next-pressed
  {:events [:multiaccounts.recover/enter-phrase-next-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [passphrase]} (:intro-wizard db)]
    {::multiaccounts/validate-mnemonic [passphrase
                                        #(re-frame/dispatch [:multiaccounts.recover/phrase-validated
                                                             %])]}))
