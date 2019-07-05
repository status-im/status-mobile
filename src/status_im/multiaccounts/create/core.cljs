(ns status-im.multiaccounts.create.core
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.multiaccounts.core :as multiaccounts.core]
            [status-im.multiaccounts.login.core :as multiaccounts.login]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.constants :as constants]
            [status-im.data-store.multiaccounts :as multiaccounts-store]
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
            [status-im.utils.platform :as platform]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ui.components.colors :as colors]
            [status-im.ethereum.core :as ethereum]))

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

(defn create-multiaccount! [{:keys [id password]}]
  (if id
    (status/multiaccount-store-derived
     id
     [constants/path-whisper constants/path-default-wallet]
     password
     #(re-frame/dispatch [:multiaccounts.create.callback/create-multiaccount-success password]))
    (status/create-multiaccount
     password
     #(re-frame/dispatch [:multiaccounts.create.callback/create-multiaccount-success (types/json->clj %) password]))))

(defn create-multiaccount
  [{:keys [db] :as   cofx}]
  (if (:intro-wizard db)
    (let [{:keys [selected-id key-code]} (:intro-wizard db)]
      (fx/merge
       cofx
       {:multiaccounts.create/create-multiaccount {:id selected-id
                                                   :password (get-in db [:multiaccounts/create :password] key-code)}}))
    (fx/merge
     cofx
     {:db (-> db
              (update :multiaccounts/create assoc
                      :id  (get-in db [:intro-wizard :selected-id])
                      :password (or (get-in db [:multiaccounts/create :password])
                                    (get-in db [:intro-wizard :key-code]))
                      :step :multiaccount-creating
                      :error nil)
              (assoc :node/on-ready :create-multiaccount
                     :multiaccounts/new-installation-id (random/guid)))}
     (node/initialize nil))))

(fx/defn add-multiaccount
  "Takes db and new multiaccount, creates map of effects describing adding multiaccount to database and realm"
  [cofx {:keys [address] :as multiaccount}]
  (let [db (:db cofx)]
    {:db                 (assoc-in db [:multiaccounts/multiaccounts address] multiaccount)
     :data-store/base-tx [(multiaccounts-store/save-multiaccount-tx multiaccount)]}))

(defn reset-multiaccount-creation [{db :db}]
  {:db (update db :multiaccounts/create assoc
               :step :enter-password
               :password nil
               :password-confirm nil
               :error nil)})

(fx/defn multiaccount-set-input-text
  [{db :db} input-key text]
  {:db (update db :multiaccounts/create merge {input-key text :error nil})})

(defn multiaccount-set-name
  [{{:multiaccounts/keys [create] :as db} :db now :now :as cofx}]
  (fx/merge cofx
            {:db                                              db
             :notifications/request-notifications-permissions nil
             :dispatch-n                                      [[:navigate-to :home]
                                                               (when-not platform/desktop?
                                                                 [:navigate-to :welcome])]}
            ;; We set last updated as we are actually changing a field,
            ;; unlike on recovery where the name is not set
            (multiaccounts.update/multiaccount-update {:last-updated now
                                                       :name         (:name create)} {})
            (mobile-network/on-network-status-change)))

(fx/defn next-step
  [{:keys [db] :as cofx} step password password-confirm]
  (case step
    :enter-password {:db (assoc-in db [:multiaccounts/create :step] :confirm-password)}
    :confirm-password (if (= password password-confirm)
                        (create-multiaccount cofx)
                        {:db (assoc-in db [:multiaccounts/create :error] (i18n/label :t/password_error1))})
    :enter-name (multiaccount-set-name cofx)))

(fx/defn step-back
  [cofx step]
  (case step
    :enter-password (navigation/navigate-back cofx)
    :confirm-password (reset-multiaccount-creation cofx)))

(fx/defn navigate-to-create-multiaccount-screen
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (update db :multiaccounts/create
                         #(-> %
                              (assoc :step :enter-password)
                              (dissoc :password :password-confirm :name :error)))}
            (navigation/navigate-to-cofx :create-multiaccount nil)))

(fx/defn intro-wizard
  {:events [:multiaccounts.create.ui/intro-wizard]}
  [{:keys [db] :as cofx} first-time-setup?]
  (fx/merge {:db (assoc db :intro-wizard {:step :generate-key
                                          :weak-password? true
                                          :encrypt-with-password? true
                                          :first-time-setup? first-time-setup?}
                        :multiaccounts/new-installation-id (random/guid))}
            (navigation/navigate-to-cofx :intro-wizard nil)))

(fx/defn intro-step-back
  {:events [:intro-wizard/step-back-pressed]}
  [{:keys [db] :as cofx}]
  (let  [step (get-in db [:intro-wizard :step])
         first-time-setup? (get-in db [:intro-wizard :first-time-setup?])]
    (if (not= :generate-key step)
      (fx/merge {:db (cond-> (assoc-in db [:intro-wizard :step] (dec-step step))
                       (#{:create-code :confirm-code} step)
                       (update :intro-wizard assoc :weak-password? true :key-code nil)
                       (= step :confirm-code)
                       (assoc-in [:intro-wizard :confirm-failure?] false))}
                (navigation/navigate-to-cofx :intro-wizard nil))

      (fx/merge {:db (dissoc db :intro-wizard)}
                (navigation/navigate-to-clean (if first-time-setup? :intro :multiaccounts) nil)))))

(fx/defn exit-wizard [{:keys [db] :as cofx}]
  (fx/merge {:db (dissoc db :intro-wizard)}
            (navigation/navigate-to-cofx :home nil)))

(fx/defn init-key-generation [{:keys [db] :as cofx}]
  (let [node-started? (= :started (:node/status db))]
    (fx/merge
     {:db (-> db
              (assoc-in [:intro-wizard :generating-keys?] true)
              (assoc :node/on-ready :start-onboarding))}
     (if node-started?
       {:intro-wizard/start-onboarding nil}
       (node/initialize nil)))))

(fx/defn on-confirm-failure [{:keys [db] :as cofx}]
  (do
    (utils/vibrate)
    {:db (assoc-in db [:intro-wizard :confirm-failure?] true)}))

(defn confirm-failure? [db]
  (let [step (get-in db [:intro-wizard :step])]
    (and (= step :confirm-code)
         (not (:multiaccounts/login db))
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
  (let  [step (get-in db [:intro-wizard :step])
         first-time-setup? (get-in db [:intro-wizard :first-time-setup?])]
    (cond (confirm-failure? db)
          (on-confirm-failure cofx)

          (or (= step :enable-notifications)
              (and (not first-time-setup?) (= step :confirm-code)
                   (:multiaccounts/login db)))
          (fx/merge cofx
                    (when (and (= step :enable-notifications) (not skip?))
                      {:notifications/request-notifications-permissions nil})
                    exit-wizard)

          (= step :generate-key)
          (init-key-generation cofx)

          (= step :create-code)
          (store-key-code cofx)

          (and (= step :confirm-code)
               (not (:multiaccounts/login db)))
          (create-multiaccount cofx)

          :else {:db (assoc-in db [:intro-wizard :step]
                               (inc-step step))})))

(defn prepare-default-account [{:keys [publicKey address]}]
  {:public-key publicKey
   :address    address
   :color      colors/blue
   :default?   true
   :name       "Status account"})

(fx/defn on-multiaccount-created
  [{:keys [signing-phrase db] :as cofx}
   {:keys [keycard-instance-uid keycard-key-uid keycard-pairing keycard-paired-on mnemonic] :as multiaccount}
   password
   {:keys [seed-backed-up? login?] :or {login? true}}]
  (let [{:keys [publicKey address]} (get-in multiaccount [:derived constants/path-whisper-keyword])
        default-wallet-account (get-in multiaccount [:derived constants/path-default-wallet-keyword])
        {:networks/keys [networks]} db
        new-multiaccount       {;;multiaccount
                                :root-address               (:address multiaccount)
                                :public-key                 publicKey
                                :installation-id            (get-in db [:multiaccounts/new-installation-id]) ;;TODO why can't we generate it here?
                                :address                    address
                                :name                       (gfycat/generate-gfy publicKey)
                                :photo-path                 (identicon/identicon publicKey)
                                :network                    config/default-network
                                :networks                   networks

                                :accounts                   [(prepare-default-account
                                                              (get-in multiaccount [:derived constants/path-default-wallet-keyword]))]

                                ;;multiaccount-settings
                                :signed-up?                 true ;; how account can be not signed?
                                :seed-backed-up?            seed-backed-up?
                                :desktop-notifications?     false
                                :signing-phrase             signing-phrase
                                :mnemonic                   mnemonic
                                :settings                   (constants/default-multiaccount-settings)
                                :syncing-on-mobile-network? false
                                :remember-syncing-choice?   false

                                ;;keycard
                                :keycard-instance-uid       keycard-instance-uid
                                :keycard-key-uid            keycard-key-uid
                                :keycard-pairing            keycard-pairing
                                :keycard-paired-on          keycard-paired-on}]
    (when-not (string/blank? publicKey)
      (fx/merge cofx
                {:db (assoc db :multiaccounts/login {:address      address
                                                     :main-account (:address default-wallet-account)
                                                     :password     password
                                                     :processing   true})}
                (add-multiaccount new-multiaccount)
                (when login?
                  (multiaccounts.login/user-login))
                (when (:intro-wizard db)
                  (intro-step-forward {}))))))

(re-frame/reg-fx
 :intro-wizard/start-onboarding
 (fn []
   (status/multiaccount-generate-and-derive-addresses
    5
    12
    [constants/path-whisper constants/path-default-wallet]
    #(re-frame/dispatch [:intro-wizard/on-keys-generated (types/json->clj %)]))))

(fx/defn on-keys-generated
  {:events [:intro-wizard/on-keys-generated]}
  [{:keys [db] :as cofx} result]
  (fx/merge
   {:db (update db :intro-wizard
                (fn [data]
                  (-> data
                      (dissoc :generating-keys?)
                      (assoc :multiaccounts result
                             :selected-storage-type :default
                             :selected-id (-> result first :id)
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

(re-frame/reg-cofx
 :multiaccounts.create/get-signing-phrase
 (fn [cofx _]
   (get-signing-phrase cofx)))

(re-frame/reg-fx
 :multiaccounts.create/create-multiaccount
 create-multiaccount!)
