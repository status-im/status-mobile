(ns status-im.accounts.create.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.accounts.core :as accounts.core]
            [status-im.accounts.login.core :as accounts.login]
            [status-im.accounts.statuses :as statuses]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.constants :as constants]
            [status-im.data-store.accounts :as accounts-store]
            [status-im.i18n :as i18n]
            [status-im.native-module.core :as status]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.random :as random]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.hex :as utils.hex]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.signing-phrase.core :as signing-phrase]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [clojure.set :refer [map-invert]]
            [status-im.utils.fx :as fx]
            [status-im.node.core :as node]
            [status-im.ui.screens.mobile-network-settings.events :as mobile-network]
            [status-im.utils.platform :as platform]))

(defn get-signing-phrase [cofx]
  (assoc cofx :signing-phrase (signing-phrase/generate)))

(def step-kw-to-num
  {:generate-key         1
   :choose-key           2
   :select-key-storage   3
   :create-code          4
   :confirm-code         5
   :enable-fingerprint   6
   :enable-notifications 7})

(defn dec-step [step]
  (let [inverted  (map-invert step-kw-to-num)]
    (inverted (dec (step-kw-to-num step)))))

(defn inc-step [step]
  (let [inverted  (map-invert step-kw-to-num)]
    (inverted (inc (step-kw-to-num step)))))

(defn get-status [cofx]
  (assoc cofx :status (rand-nth statuses/data)))

(defn create-account! [{:keys [id password]}]
  (if id
    (status/import-onboarding-account
     id
     password
     #(re-frame/dispatch [:accounts.create.callback/create-account-success (types/json->clj %) password]))
    (status/create-account
     password
     #(re-frame/dispatch [:accounts.create.callback/create-account-success (types/json->clj %) password]))))

;;;; Handlers
(defn create-account
  [{:keys [db] :as   cofx}]
  (if (:intro-wizard db)
    (fx/merge
     cofx
     {:accounts.create/create-account {:id  (get-in db [:intro-wizard :selected-id])
                                       :password (or (get-in db [:accounts/create :password])
                                                     (get-in db [:intro-wizard :key-code]))}})
    (fx/merge
     cofx
     {:db (-> db
              (update :accounts/create assoc
                      :id  (get-in db [:intro-wizard :selected-id])
                      :password (or (get-in db [:accounts/create :password])
                                    (get-in db [:intro-wizard :key-code]))
                      :step :account-creating
                      :error nil)
              (assoc :node/on-ready :create-account
                     :accounts/new-installation-id (random/guid)))}
     (node/initialize nil))))

(fx/defn add-account
  "Takes db and new account, creates map of effects describing adding account to database and realm"
  [cofx {:keys [address] :as account}]
  (let [db (:db cofx)
        {:networks/keys [networks]} db
        enriched-account (assoc account
                                :network config/default-network
                                :networks networks
                                :address address)]
    {:db                 (assoc-in db [:accounts/accounts address] enriched-account)
     :data-store/base-tx [(accounts-store/save-account-tx enriched-account)]}))

(defn reset-account-creation [{db :db}]
  {:db (update db :accounts/create assoc
               :step :enter-password
               :password nil
               :password-confirm nil
               :error nil)})

(fx/defn account-set-input-text
  [{db :db} input-key text]
  {:db (update db :accounts/create merge {input-key text :error nil})})

(defn account-set-name [{{:accounts/keys [create] :as db} :db now :now :as cofx}]
  (fx/merge cofx
            {:db                                              db
             :notifications/request-notifications-permissions nil
             :dispatch-n                                      [[:navigate-to :home]
                                                               (when-not platform/desktop?
                                                                 [:navigate-to :welcome])]}
            ;; We set last updated as we are actually changing a field,
            ;; unlike on recovery where the name is not set
            (accounts.update/account-update {:last-updated now
                                             :name         (:name create)} {})
            (mobile-network/on-network-status-change)))

(fx/defn next-step
  [{:keys [db] :as cofx} step password password-confirm]
  (case step
    :enter-password {:db (assoc-in db [:accounts/create :step] :confirm-password)}
    :confirm-password (if (= password password-confirm)
                        (create-account cofx)
                        {:db (assoc-in db [:accounts/create :error] (i18n/label :t/password_error1))})
    :enter-name (account-set-name cofx)))

(fx/defn step-back
  [cofx step]
  (case step
    :enter-password (navigation/navigate-back cofx)
    :confirm-password (reset-account-creation cofx)))

(fx/defn navigate-to-create-account-screen
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (update db :accounts/create
                         #(-> %
                              (assoc :step :enter-password)
                              (dissoc :password :password-confirm :name :error)))}
            (navigation/navigate-to-cofx :create-account nil)))

(fx/defn intro-wizard
  {:events [:accounts.create.ui/intro-wizard]}
  [{:keys [db] :as cofx}]
  (fx/merge {:db (assoc db :intro-wizard {:step :generate-key
                                          :weak-password? true
                                          :encrypt-with-password? true}
                        :accounts/new-installation-id (random/guid))}
            (navigation/navigate-to-cofx :intro-wizard nil)))

(fx/defn intro-step-back
  {:events [:intro-wizard/step-back-pressed]}
  [{:keys [db] :as cofx}]
  (let  [step (get-in db [:intro-wizard :step])]
    (if (not= :generate-key step)
      (fx/merge {:db (cond-> (assoc-in db [:intro-wizard :step] (dec-step step))
                       (#{:create-code :confirm-code} step)
                       (update :intro-wizard assoc :weak-password? true :key-code nil)
                       (= step :confirm-code)
                       (assoc-in [:intro-wizard :confirm-failure?] false))}
                (navigation/navigate-to-cofx :intro-wizard nil))

      (fx/merge {:db (dissoc db :intro-wizard)}
                (navigation/navigate-to-clean :intro nil)))))

(fx/defn exit-wizard [{:keys [db] :as cofx}]
  (fx/merge {:db (dissoc db :intro-wizard)
             :notifications/request-notifications-permissions nil}
            (navigation/navigate-to-cofx :home nil)))

(fx/defn init-key-generation [{:keys [db] :as cofx}]
  (fx/merge
   {:db (-> db
            (assoc-in [:intro-wizard :generating-keys?] true)
            (assoc :node/on-ready :start-onboarding))}
   (node/initialize nil)))

(fx/defn on-confirm-failure [{:keys [db] :as cofx}]
  (do
    (utils/vibrate)
    {:db (assoc-in db [:intro-wizard :confirm-failure?] true)}))

(defn confirm-failure? [db]
  (let [step (get-in db [:intro-wizard :step])]
    (and (= step :confirm-code)
         (not (:accounts/login db))
         (get-in db [:intro-wizard :encrypt-with-password?])
         (not= (get-in db [:intro-wizard :stored-key-code]) (get-in db [:intro-wizard :key-code])))))

(fx/defn store-key-code [{:keys [db] :as cofx}]
  (let [key-code  (get-in db [:intro-wizard :key-code])]
    {:db (update db :intro-wizard
                 assoc :stored-key-code key-code
                 :key-code nil
                 :step :confirm-code)}))

(fx/defn intro-step-forward
  {:events [:intro-wizard/step-forward-pressed]}
  [{:keys [db] :as cofx} {:keys [skip?] :as opts}]
  (let  [step (get-in db [:intro-wizard :step])]
    (cond (= step :enable-notifications)
          (exit-wizard cofx)

          (= step :generate-key)
          (init-key-generation cofx)

          (confirm-failure? db)
          (on-confirm-failure cofx)

          (= step :create-code)
          (store-key-code cofx)

          :else (fx/merge {:db (assoc-in db [:intro-wizard :step]
                                         (inc-step step))}
                          (when (and (= step :confirm-code)
                                     (not (:accounts/login db)))
                            (create-account cofx))))))

(fx/defn on-account-created
  [{:keys [signing-phrase
           status
           db] :as cofx}
   {:keys [pubkey address mnemonic installation-id
           keycard-instance-uid keycard-key-uid keycard-pairing keycard-paired-on] :as result}
   password
   {:keys [seed-backed-up? login? new-account?] :or {login? true}}]
  (let [normalized-address (utils.hex/normalize-hex address)
        account            {:public-key             pubkey
                            :installation-id        (or installation-id (get-in db [:accounts/new-installation-id]))
                            :address                normalized-address
                            :name                   (gfycat/generate-gfy pubkey)
                            :status                 status
                            :signed-up?             true
                            :desktop-notifications? false
                            :photo-path             (identicon/identicon pubkey)
                            :signing-phrase         signing-phrase
                            :seed-backed-up?        seed-backed-up?
                            :mnemonic               mnemonic
                            :keycard-instance-uid   keycard-instance-uid
                            :keycard-key-uid        keycard-key-uid
                            :keycard-pairing        keycard-pairing
                            :keycard-paired-on      keycard-paired-on
                            :settings               (constants/default-account-settings)
                            :syncing-on-mobile-network? false
                            :remember-syncing-choice? false
                            :new-account?           new-account?}]
    (when-not (string/blank? pubkey)
      (fx/merge cofx
                {:db (assoc db :accounts/login {:address    normalized-address
                                                :password   password
                                                :processing true})}
                (add-account account)
                (when login?
                  (accounts.login/user-login true))
                (when (:intro-wizard db)
                  (intro-step-forward {}))))))

(re-frame/reg-fx
 :intro-wizard/start-onboarding
 (fn [{:keys [n mnemonic-length]}]
   (status/start-onboarding n mnemonic-length
                            #(re-frame/dispatch [:intro-wizard/on-keys-generated (types/json->clj %)]))))

(fx/defn on-keys-generated
  {:events [:intro-wizard/on-keys-generated]}
  [{:keys [db] :as cofx} result]
  (fx/merge
   {:db (update db :intro-wizard
                (fn [data]
                  (-> data
                      (dissoc :generating-keys?)
                      (assoc :accounts (:accounts result)
                             :selected-storage-type :default
                             :selected-id (-> result :accounts first :id)
                             :step :choose-key))))}
   (navigation/navigate-to-cofx :intro-wizard nil)))

(fx/defn on-key-selected
  {:events [:intro-wizard/on-key-selected]}
  [{:keys [db] :as cofx} id]
  {:db (assoc-in db [:intro-wizard :selected-id] id)})

(fx/defn on-key-storage-selected
  {:events [:intro-wizard/on-key-storage-selected]}
  [{:keys [db] :as cofx} storage-type]
  {:db (assoc-in db [:intro-wizard :selected-storage-type] storage-type)})

(fx/defn on-encrypt-with-password-pressed
  {:events [:intro-wizard/on-encrypt-with-password-pressed]}
  [{:keys [db] :as cofx}]
  {:db (assoc-in db [:intro-wizard :encrypt-with-password?] true)})

(fx/defn on-learn-more-pressed
  {:events [:intro-wizard/on-learn-more-pressed]}
  [{:keys [db] :as cofx}]
  {:db (assoc-in db [:intro-wizard :show-learn-more?] true)})

(defn get-new-key-code [current-code sym encrypt-with-password?]
  (cond (or (= sym :remove) (= sym "Backspace"))
        (subs current-code 0 (dec (count current-code)))
        (and (not encrypt-with-password?) (= (count current-code) 6))
        current-code
        (= (count sym) 1)
        (str current-code sym)
        :else current-code))

(fx/defn code-symbol-pressed
  {:events [:intro-wizard/code-symbol-pressed]}
  [{:keys [db] :as cofx} new-key-code]
  (let [encrypt-with-password? (get-in db [:intro-wizard :encrypt-with-password?])]
    {:db (update db :intro-wizard assoc :key-code new-key-code
                 :confirm-failure? false
                 :weak-password? (< (count new-key-code) 6))}))

;;;; COFX

(re-frame/reg-cofx
 :accounts.create/get-signing-phrase
 (fn [cofx _]
   (get-signing-phrase cofx)))

(re-frame/reg-cofx
 :accounts.create/get-status
 (fn [cofx _]
   (get-status cofx)))

;;;; FX

(re-frame/reg-fx
 :accounts.create/create-account
 create-account!)
