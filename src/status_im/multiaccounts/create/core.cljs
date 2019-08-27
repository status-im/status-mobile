(ns status-im.multiaccounts.create.core
  (:require [clojure.set :refer [map-invert]]
            [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.native-module.core :as status]
            [status-im.node.core :as node]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.mobile-network-settings.events :as mobile-network]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.config :as config]
            [status-im.utils.fx :as fx]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.platform :as platform]
            [status-im.utils.random :as random]
            [status-im.utils.security :as security]
            [status-im.utils.signing-phrase.core :as signing-phrase]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [taoensso.timbre :as log]))

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

;; multiaccounts create module
(defn get-selected-multiaccount [{:keys [db]}]
  (let [{:keys [selected-id multiaccounts]} (:intro-wizard db)]
    (some #(when (= selected-id (:id %)) %) multiaccounts)))

(fx/defn create-multiaccount
  [{:keys [db] :as cofx}]
  (let [{:keys [selected-id address key-code]} (:intro-wizard db)
        {:keys [address]} (get-selected-multiaccount cofx)
        callback #(re-frame/dispatch [::store-multiaccount-success key-code])]
    {::store-multiaccount [selected-id address key-code callback]}))

(fx/defn intro-wizard
  {:events [:multiaccounts.create.ui/intro-wizard]}
  [{:keys [db] :as cofx} first-time-setup?]
  (fx/merge {:db (assoc db :intro-wizard {:step :generate-key
                                          :weak-password? true
                                          :encrypt-with-password? true
                                          :first-time-setup? first-time-setup?})}
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
                (navigation/navigate-back)))))

(fx/defn exit-wizard [{:keys [db] :as cofx}]
  (fx/merge {:db (dissoc db :intro-wizard)}
            (navigation/navigate-to-cofx :home nil)))

(fx/defn init-key-generation
  [{:keys [db] :as cofx}]
  {:db (assoc-in db [:intro-wizard :generating-keys?] true)
   :intro-wizard/start-onboarding nil})

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
  (let  [{:keys [step first-time-setup? selected-storage-type processing?]} (:intro-wizard db)]
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
               (not (:multiaccounts/login db))
               (not processing?))
          (fx/merge cofx
                    {:db (assoc-in db [:intro-wizard :processing?] true)}
                    create-multiaccount)

          (and (= step :select-key-storage)
               (= :advanced selected-storage-type))
          {:dispatch [:keycard/start-onboarding-flow]}

          :else {:db (update db :intro-wizard
                             assoc :processing? false
                             :step (inc-step step))})))

(defn prepare-accounts-data
  [multiaccount]
  [(let [{:keys [publicKey address]}
         (get-in multiaccount [:derived constants/path-default-wallet-keyword])]
     {:publicKey publicKey
      :address    address
      :color      colors/blue
      :wallet     true
      :path       constants/path-default-wallet
      :name       "Status account"})
   (let [{:keys [publicKey address]}
         (get-in multiaccount [:derived constants/path-whisper-keyword])]
     {:publicKey publicKey
      :address    address
      :path       constants/path-whisper
      :chat       true})])

(fx/defn on-multiaccount-created
  [{:keys [signing-phrase random-guid-generator db] :as cofx}
   {:keys [address publicKey keycard-instance-uid keycard-key-uid keycard-pairing keycard-paired-on mnemonic] :as multiaccount}
   password
   {:keys [seed-backed-up? login?] :or {login? true}}]
  (let [[wallet-account {:keys [publicKey]} :as accounts-data] (prepare-accounts-data multiaccount)
        name (gfycat/generate-gfy publicKey)
        photo-path (identicon/identicon publicKey)
        multiaccount-data {:name name :address address :photo-path photo-path}
        new-multiaccount       {:address         address
                                :name            name
                                :photo-path      photo-path
                                :public-key      publicKey

                                :latest-derived-path 0
                                :accounts [wallet-account]
                                :signing-phrase  signing-phrase

                                :installation-id (random-guid-generator)
                                :mnemonic        mnemonic
                                :settings        constants/default-multiaccount-settings}
        db (assoc db
                  :multiaccounts/login {:address      address
                                        :name         name
                                        :photo-path   photo-path
                                        :password     password
                                        :creating?    true
                                        :processing   true}
                  :multiaccount new-multiaccount
                  :networks/current-network constants/default-network
                  :networks/networks constants/default-networks)]
    (fx/merge cofx
              {:db (cond-> db
                     seed-backed-up?
                     (assoc-in [:multiaccount :seed-backed-up?] true))
               ::save-account-and-login [(types/clj->json multiaccount-data)
                                         password
                                         (node/get-new-config db)
                                         (types/clj->json accounts-data)]}
              (when (:intro-wizard db)
                (intro-step-forward {})))))

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
 ::get-signing-phrase
 (fn [cofx _]
   (get-signing-phrase cofx)))

(fx/defn create-multiaccount-success
  {:events [::store-multiaccount-success]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)
                  (re-frame/inject-cofx ::get-signing-phrase)]}
  [cofx password]
  (on-multiaccount-created cofx (get-selected-multiaccount cofx) password {:seed-backed-up? false}))

(re-frame/reg-fx
 ::store-multiaccount
 (fn [[id address password callback]]
   (status/multiaccount-store-account
    id
    (security/safe-unmask-data password)
    (fn []
      (status/multiaccount-load-account
       address
       password
       (fn [value]
         (let [{:keys [id]} (types/json->clj value)]
           (status/multiaccount-store-derived
            id
            [constants/path-whisper constants/path-default-wallet]
            password
            callback))))))))

(re-frame/reg-fx
 ::save-account-and-login
 (fn [[multiaccount-data password config accounts-data]]
   (status/save-account-and-login multiaccount-data
                                  (security/safe-unmask-data password)
                                  config
                                  accounts-data)))
