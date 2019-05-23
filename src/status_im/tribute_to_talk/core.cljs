(ns status-im.tribute-to-talk.core
  (:refer-clojure :exclude [remove])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.contact.db :as contact.db]
            [status-im.ethereum.contracts :as contracts]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ipfs.core :as ipfs]
            [status-im.tribute-to-talk.db :as tribute-to-talk.db]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.contenthash :as contenthash]
            [status-im.utils.fx :as fx]
            [status-im.wallet.core :as wallet]
            [taoensso.timbre :as log]))

(fx/defn update-settings
  [{:keys [db] :as cofx} {:keys [snt-amount message update] :as new-settings}]
  (let [account-settings (get-in db [:account/account :settings])
        chain-keyword    (ethereum/chain-keyword db)
        tribute-to-talk-settings (cond-> (merge (tribute-to-talk.db/get-settings db)
                                                new-settings)
                                   new-settings
                                   (assoc :seen? true)

                                   (not new-settings)
                                   (dissoc :snt-amount :manifest)

                                   (and (contains? new-settings :update)
                                        (nil? update))
                                   (dissoc :update))]
    (accounts.update/update-settings
     cofx
     (-> account-settings
         (assoc-in [:tribute-to-talk chain-keyword]
                   tribute-to-talk-settings))
     {})))

(fx/defn mark-ttt-as-seen
  [{:keys [db] :as cofx}]
  (when-not (:seen (tribute-to-talk.db/get-settings db))
    (update-settings cofx {:seen? true})))

(fx/defn open-settings
  [{:keys [db] :as cofx}]
  (let [settings (tribute-to-talk.db/get-settings db)
        updated-settings (:update settings)]
    (fx/merge cofx
              mark-ttt-as-seen
              (navigation/navigate-to-cofx
               :tribute-to-talk
               (cond
                 updated-settings
                 (merge {:step :finish}
                        updated-settings
                        (when updated-settings
                          {:state :pending}))
                 (:snt-amount settings)
                 (merge {:step :edit
                         :editing? true}
                        (update settings :snt-amount tribute-to-talk.db/from-wei))
                 :else
                 {:step :intro})))))

(fx/defn set-step
  [{:keys [db]} step]
  {:db (assoc-in db [:navigation/screen-params :tribute-to-talk :step] step)})

(fx/defn set-step-finish
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc-in db [:navigation/screen-params :tribute-to-talk :state] :signing)}
            (set-step :finish)))

(fx/defn open-learn-more
  [cofx]
  (set-step cofx :learn-more))

(fx/defn step-back
  [cofx]
  (let [{:keys [step editing?]}
        (get-in cofx [:db :navigation/screen-params :tribute-to-talk])]
    (case step
      (:intro :edit)
      (navigation/navigate-back cofx)

      (:learn-more :set-snt-amount)
      (set-step cofx (if editing?
                       :edit
                       :intro))

      :personalized-message
      (set-step cofx :set-snt-amount)

      :finish
      (set-step cofx :personalized-message))))

(fx/defn upload-manifest
  [cofx]
  (let [{:keys [message snt-amount]}
        (get-in cofx [:db :navigation/screen-params :tribute-to-talk])
        manifest {:tribute-to-talk
                  {:message message
                   :snt-amount (tribute-to-talk.db/to-wei snt-amount)}}]
    (ipfs/add cofx
              {:value (js/JSON.stringify
                       (clj->js manifest))
               :on-success
               (fn [response]
                 [:tribute-to-talk.callback/manifest-uploaded
                  (:hash response)])
               :on-failure
               (fn [error]
                 [:tribute-to-talk.callback/manifest-upload-failed error])})))

(fx/defn step-forward
  [cofx]
  (let [{:keys [step editing?]}
        (get-in cofx [:db :navigation/screen-params :tribute-to-talk])]
    (case step
      :intro
      (set-step cofx :set-snt-amount)

      :set-snt-amount
      (set-step cofx :personalized-message)

      :personalized-message
      (fx/merge cofx
                (set-step-finish)
                (upload-manifest))

      :finish
      (navigation/navigate-back cofx))))

(defn get-new-snt-amount
  [snt-amount numpad-symbol]
  ;; TODO: Put some logic in place so that incorrect numbers can not
  ;; be entered
  (let [snt-amount  (or (str snt-amount) "0")]
    (if (= numpad-symbol :remove)
      (let [len (count snt-amount)
            s (subs snt-amount 0 (dec len))]
        (cond-> s
          ;; Remove both the digit after the dot and the dot itself
          (string/ends-with? s ".") (subs 0 (- len 2))
          ;; Set default value if last digit is removed
          (string/blank? s) (do "0")))
      (cond
        ;; Disallow two consecutive dots
        (and (string/includes? snt-amount ".") (= numpad-symbol "."))
        snt-amount
        ;; Disallow more than 2 digits after the dot
        (and (string/includes? snt-amount ".")
             (> (count (second (string/split snt-amount #"\."))) 1))
        snt-amount
        ;; Replace initial "0" by the first digit
        (and (= snt-amount "0") (not= numpad-symbol "."))
        (str numpad-symbol)
        :else (str snt-amount numpad-symbol)))))

(fx/defn update-snt-amount
  [{:keys [db]} numpad-symbol]
  {:db (update-in db
                  [:navigation/screen-params :tribute-to-talk :snt-amount]
                  #(get-new-snt-amount % numpad-symbol))})

(fx/defn update-message
  [{:keys [db]} message]
  {:db (assoc-in db
                 [:navigation/screen-params :tribute-to-talk :message]
                 message)})

(fx/defn start-editing
  [{:keys [db]}]
  {:db (assoc-in db
                 [:navigation/screen-params :tribute-to-talk :step]
                 :set-snt-amount)})

(fx/defn fetch-manifest
  [{:keys [db] :as cofx} identity contenthash]
  (contenthash/cat cofx
                   {:contenthash contenthash
                    :on-failure
                    (fn [error]
                      (re-frame/dispatch
                       (if (= 503 error)
                         [:tribute-to-talk.callback/fetch-manifest-failure
                          identity contenthash]
                         [:tribute-to-talk.callback/no-manifest-found identity])))
                    :on-success
                    (fn [manifest-json]
                      (let [manifest (js->clj (js/JSON.parse manifest-json)
                                              :keywordize-keys true)]
                        (re-frame/dispatch
                         [:tribute-to-talk.callback/fetch-manifest-success
                          identity manifest])))}))

(re-frame/reg-fx
 :tribute-to-talk/get-manifest
 (fn [{:keys [contract address on-success]}]
   (json-rpc/eth-call
    {:contract contract
     :method "getManifest(address)"
     :params [address]
     :outputs ["bytes"]
     :on-success on-success})))

(fx/defn check-manifest
  [{:keys [db] :as cofx} public-key]
  (if-let [contract (contracts/get-address db :status/tribute-to-talk)]
    (let [address (contact.db/public-key->address public-key)]
      {:tribute-to-talk/get-manifest
       {:contract contract
        :address  address
        :on-success
        (fn [[contenthash]]
          (re-frame/dispatch
           (if contenthash
             [:tribute-to-talk.callback/check-manifest-success
              public-key
              contenthash]
             [:tribute-to-talk.callback/no-manifest-found public-key])))}})
    ;; update settings if checking own manifest or do nothing otherwise
    (when-let [me? (= identity
                      (get-in cofx [:db :account/account :public-key]))]
      (update-settings cofx nil))))

(fx/defn check-own-manifest
  [cofx]
  (check-manifest cofx (get-in cofx [:db :account/account :public-key])))

(fx/defn set-manifest-signing-flow
  [{:keys [db] :as cofx} hash]
  (let [contenthash (when hash
                      (contenthash/encode {:hash hash
                                           :namespace :ipfs}))]
    (if-let [contract (contracts/get-address db :status/tribute-to-talk)]
      (wallet/eth-transaction-call
       cofx
       {:contract contract
        :method "setManifest(bytes)"
        :params [contenthash]
        :on-result [:tribute-to-talk.callback/set-manifest-transaction-completed]
        :on-error [:tribute-to-talk.callback/set-manifest-transaction-failed]})
      {:db (assoc-in db
                     [:navigation/screen-params :tribute-to-talk :state]
                     :transaction-failed)})))

(fx/defn remove
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc-in db [:navigation/screen-params :tribute-to-talk]
                           {:step :finish
                            :state :disabled})}
            (set-manifest-signing-flow nil)))

(fx/defn check-set-manifest-transaction
  [{:keys [db] :as cofx}]
  (let [transaction (get-in (tribute-to-talk.db/get-settings db) [:update :transaction])]
    (when transaction
      (let [confirmed? (pos? (js/parseInt
                              (get-in cofx [:db :wallet :transactions
                                            transaction :confirmations]
                                      0)))
            ;;TODO support failed transactions
            failed? false]
        (cond
          failed?
          (fx/merge cofx
                    {:db (assoc-in db [:navigation/screen-params :tribute-to-talk :state] :transaction-failed)}
                    (update-settings {:update nil}))

          confirmed?
          (fx/merge cofx
                    {:db (assoc-in db [:navigation/screen-params :tribute-to-talk :state] :completed)}
                    check-own-manifest
                    (update-settings {:update nil}))

          (not confirmed?)
          {:dispatch-later [{:ms       10000
                             :dispatch [:tribute-to-talk/check-set-manifest-transaction-timeout]}]})))))

(fx/defn on-set-manifest-transaction-completed
  [{:keys [db] :as cofx} transaction-hash]
  (let [{:keys [snt-amount message]} (get-in db [:navigation/screen-params
                                                 :tribute-to-talk])]
    (fx/merge cofx
              {:db (assoc-in db [:navigation/screen-params :tribute-to-talk :state] :pending)}
              (navigation/navigate-to-clean :wallet-transaction-sent-modal {})
              (update-settings {:update {:transaction transaction-hash
                                         :snt-amount  snt-amount
                                         :message     message}})
              check-set-manifest-transaction)))

(fx/defn on-set-manifest-transaction-failed
  [{:keys [db] :as cofx} error]
  (log/error :set-manifest-transaction-failed error)
  {:db (assoc-in db
                 [:navigation/screen-params :tribute-to-talk :state]
                 :transaction-failed)})

(fx/defn on-manifest-upload-failed
  [{:keys [db] :as cofx} error]
  (log/error :upload-manifest-failed error)
  {:db (assoc-in db
                 [:navigation/screen-params :tribute-to-talk :state]
                 :transaction-failed)})

(fx/defn init
  [cofx]
  (fx/merge cofx
            check-own-manifest
            check-set-manifest-transaction))
