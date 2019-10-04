(ns status-im.multiaccounts.create.core
  (:require [clojure.set :refer [map-invert]]
            [re-frame.core :as re-frame]
            [status-im.constants :as constants]
            [status-im.ethereum.core :as ethereum]
            [taoensso.timbre :as log]
            [status-im.i18n :as i18n]
            [status-im.multiaccounts.db :as db]
            [status-im.native-module.core :as status]
            [status-im.node.core :as node]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.gfycat.core :as gfycat]
            [status-im.utils.identicon :as identicon]
            [status-im.utils.security :as security]
            [status-im.utils.signing-phrase.core :as signing-phrase]
            [status-im.utils.types :as types]
            [status-im.utils.utils :as utils]
            [status-im.utils.platform :as platform]))

(defn get-signing-phrase [cofx]
  (assoc cofx :signing-phrase (signing-phrase/generate)))

(def step-kw-to-num
  {:generate-key         1
   :choose-key           2
   :select-key-storage   3
   :create-code          4
   :confirm-code         5
   :enable-notifications 6})

(defn decrement-step [step]
  (let [inverted  (map-invert step-kw-to-num)]
    (if (and (= step :create-code)
             (not platform/android?))
      :choose-key
      (inverted (dec (step-kw-to-num step))))))

(defn inc-step [step]
  (let [inverted  (map-invert step-kw-to-num)]
    (if (and (= step :choose-key)
             (not platform/android?))
      :create-code
      (inverted (inc (step-kw-to-num step))))))

;; multiaccounts create module
(defn get-selected-multiaccount [{:keys [db]}]
  (let [{:keys [selected-id multiaccounts]} (:intro-wizard db)]
    (some #(when (= selected-id (:id %)) %) multiaccounts)))

(fx/defn create-multiaccount
  [{:keys [db] :as cofx}]
  (let [{:keys [selected-id address key-code]} (:intro-wizard db)
        {:keys [address]} (get-selected-multiaccount cofx)
        hashed-password (ethereum/sha3 (security/safe-unmask-data key-code))
        callback #(re-frame/dispatch [::store-multiaccount-success key-code %])]
    {::store-multiaccount [selected-id address hashed-password callback]}))

(fx/defn intro-wizard
  {:events [:multiaccounts.create.ui/intro-wizard]}
  [{:keys [db] :as cofx} first-time-setup?]
  (fx/merge cofx
            {:db (assoc db :intro-wizard {:step :generate-key
                                          :weak-password? true
                                          :back-action :intro-wizard/navigate-back
                                          :forward-action :intro-wizard/step-forward-pressed
                                          :encrypt-with-password? true
                                          :first-time-setup? first-time-setup?})
             ::navigation/add-wizard-back-event [:intro-wizard/step-back-pressed]}
            (navigation/navigate-to-cofx :create-multiaccount-generate-key nil)))

(fx/defn dec-step
  {:events [:intro-wizard/dec-step]}
  [{:keys [db] :as cofx}]
  (let  [step (get-in db [:intro-wizard :step])]
    (fx/merge cofx
              (when-not (= :generate-key step)
                {:db (cond-> (assoc-in db [:intro-wizard :step] (decrement-step step))
                       (#{:create-code :confirm-code} step)
                       (update :intro-wizard assoc :weak-password? true :key-code nil)
                       (= step :confirm-code)
                       (assoc-in [:intro-wizard :confirm-failure?] false))})
              (when (= :generate-key-step)
                {:db (dissoc db :intro-wizard)
                 ::navigation/remove-wizard-back-event nil}))))

(fx/defn navigate-back
  {:events [:intro-wizard/navigate-back]}
  [cofx]
  {::navigation/navigate-back nil})

(fx/defn intro-step-back
  {:events [:intro-wizard/step-back-pressed]}
  [{:keys [db] :as cofx} skip-alert?]
  (let  [step (get-in db [:intro-wizard :step])]
    ;; Cannot go back after account has been created
    ;; and we're on "Enable notifications" step
    (when-not (= :enable-notifications step)
      (if (and (= step :choose-key) (not skip-alert?))
        (utils/show-question
         (i18n/label :t/are-you-sure-to-cancel)
         (i18n/label :t/you-will-start-from-scratch)
         #(re-frame/dispatch [:intro-wizard/step-back-pressed true])
         #(re-frame/dispatch [:navigation/reset-processing-flag]))
        (fx/merge cofx
                  dec-step
                  navigation/navigate-back)))))

(fx/defn exit-wizard [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (dissoc db :intro-wizard)
             ::navigation/remove-wizard-back-event nil}
            (navigation/navigate-to-cofx :home nil)))

(fx/defn init-key-generation
  [{:keys [db] :as cofx}]
  {:db (assoc-in db [:intro-wizard :processing?] true)
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
                 :key-code nil)}))

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

          (and (= step :confirm-code)
               (not (:multiaccounts/login db))
               (not processing?))
          (fx/merge cofx
                    {:db (assoc-in db [:intro-wizard :processing?] true)}
                    create-multiaccount)

          (and (= step :select-key-storage)
               (= :advanced selected-storage-type))
          {:dispatch [:keycard/start-onboarding-flow]}

          :else (let [next-step (inc-step step)]
                  (fx/merge cofx
                            {:db (update db :intro-wizard
                                         assoc :processing? false
                                         :step next-step)}
                            (when (= step :create-code)
                              store-key-code)
                            (when (= next-step :enable-notifications)
                              (navigation/navigate-reset {:index 0
                                                          :actions [{:routeName :create-multiaccount-enable-notifications}]}))
                            (when (not= next-step :enable-notifications)
                              (navigation/navigate-to-cofx (->> next-step name (str "create-multiaccount-") keyword) nil)))))))

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

(fx/defn save-account-and-login-with-keycard
  [_ multiaccount-data password node-config chat-key]
  {::save-account-and-login-with-keycard [(types/clj->json multiaccount-data)
                                          password
                                          node-config
                                          chat-key]})

(fx/defn save-account-and-login
  [_ multiaccount-data password node-config accounts-data]
  {::save-account-and-login [(types/clj->json multiaccount-data)
                             password
                             node-config
                             (types/clj->json accounts-data)]})

(fx/defn on-multiaccount-created
  [{:keys [signing-phrase random-guid-generator db] :as cofx}
   {:keys [address chat-key keycard-instance-uid keycard-key-uid keycard-pairing keycard-paired-on mnemonic] :as multiaccount}
   password
   {:keys [seed-backed-up? login?] :or {login? true}}]
  (let [[wallet-account {:keys [publicKey]} :as accounts-data] (prepare-accounts-data multiaccount)
        name (gfycat/generate-gfy publicKey)
        photo-path (identicon/identicon publicKey)
        multiaccount-data {:name name :address address :photo-path photo-path}
        new-multiaccount (cond-> {; address of the master key
                                  :address             address
                                  ;; The address from which we derive any wallet
                                  :wallet-root-address (get-in multiaccount [:derived constants/path-wallet-root-keyword :address])
                                  ;; The address from which we derive any chat account/encryption keys
                                  :eip1581-address     (get-in multiaccount [:derived constants/path-eip1581-keyword :address])
                                  :name                name
                                  :photo-path          photo-path
                                  ; public key of the chat account
                                  :public-key          publicKey
                                  ; default address for Dapps
                                  :dapps-address       (:address wallet-account)
                                  :latest-derived-path 0
                                  :accounts            [wallet-account]
                                  :signing-phrase      signing-phrase

                                  :installation-id     (random-guid-generator)
                                  :mnemonic            mnemonic
                                  :settings            constants/default-multiaccount-settings}

                           keycard-key-uid (assoc :keycard-instance-uid keycard-instance-uid
                                                  :keycard-key-uid keycard-key-uid
                                                  :keycard-pairing keycard-pairing
                                                  :keycard-paired-on keycard-paired-on))
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
                     (assoc-in [:multiaccount :seed-backed-up?] true))}
              (if keycard-key-uid
                (save-account-and-login-with-keycard new-multiaccount
                                                     password
                                                     (node/get-new-config db)
                                                     chat-key)
                (save-account-and-login multiaccount-data
                                        (ethereum/sha3 (security/safe-unmask-data password))
                                        (node/get-new-config db)
                                        accounts-data))
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
                      (dissoc :processing?)
                      (assoc :multiaccounts result
                             :selected-storage-type :default
                             :selected-id (-> result first :id)
                             :step :choose-key))))}
   (navigation/navigate-to-cofx :create-multiaccount-choose-key nil)))

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
                 :weak-password? (not (db/valid-length? new-key-code)))}))

(re-frame/reg-cofx
 ::get-signing-phrase
 (fn [cofx _]
   (get-signing-phrase cofx)))

(fx/defn create-multiaccount-success
  {:events [::store-multiaccount-success]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)
                  (re-frame/inject-cofx ::get-signing-phrase)]}
  [cofx password derived]
  (on-multiaccount-created cofx
                           (assoc
                            (get-selected-multiaccount cofx)
                            :derived
                            (types/json->clj derived))
                           password
                           {:seed-backed-up? false}))

(re-frame/reg-fx
 ::store-multiaccount
 (fn [[id address hashed-password callback]]
   (status/multiaccount-store-derived
    id
    [constants/path-wallet-root
     constants/path-eip1581
     constants/path-whisper
     constants/path-default-wallet]
    hashed-password
    callback)))

(re-frame/reg-fx
 ::save-account-and-login
 (fn [[multiaccount-data hashed-password config accounts-data]]
   (status/save-account-and-login multiaccount-data
                                  hashed-password
                                  config
                                  accounts-data)))
(re-frame/reg-fx
 ::save-account-and-login-with-keycard
 (fn [[multiaccount-data password config chat-key]]
   (status/save-account-and-login-with-keycard multiaccount-data
                                               (security/safe-unmask-data password)
                                               config
                                               chat-key)))
