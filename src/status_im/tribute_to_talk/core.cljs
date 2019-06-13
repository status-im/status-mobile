(ns status-im.tribute-to-talk.core
  (:refer-clojure :exclude [remove])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as accounts.update]
            [status-im.contact.core :as contact]
            [status-im.ethereum.contracts :as contracts]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.transactions.core :as transactions]
            [status-im.tribute-to-talk.db :as tribute-to-talk.db]
            [status-im.tribute-to-talk.whitelist :as whitelist]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]
            [taoensso.timbre :as log]
            [status-im.signing.core :as signing]))

(defn add-transaction-hash
  [message db]
  (let [to (get-in message [:content :chat-id])
        tribute-transaction-hash
        (get-in db [:contacts/contacts to :tribute-to-talk :transaction-hash])]
    (if tribute-transaction-hash
      (assoc-in message
                [:content :tribute-transaction]
                tribute-transaction-hash)
      message)))

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
    (fx/merge cofx
              (accounts.update/update-settings
               (-> account-settings
                   (assoc-in [:tribute-to-talk chain-keyword]
                             tribute-to-talk-settings))
               {})
              (whitelist/enable-whitelist))))

(fx/defn mark-ttt-as-seen
  [{:keys [db] :as cofx}]
  (when-not (:seen (tribute-to-talk.db/get-settings db))
    (update-settings cofx {:seen? true})))

(fx/defn open-settings
  {:events [:tribute-to-talk.ui/menu-item-pressed]}
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
                        (update settings :snt-amount  tribute-to-talk.db/from-wei))
                 :else
                 {:step :intro
                  :snt-amount "0"})))))

(fx/defn set-step
  [{:keys [db]} step]
  {:db (assoc-in db [:navigation/screen-params :tribute-to-talk :step] step)})

(fx/defn set-tribute-signing-flow
  [{:keys [db] :as cofx} tribute]
  (if-let [contract (contracts/get-address db :status/tribute-to-talk)]
    (signing/eth-transaction-call
     cofx
     {:contract  contract
      :method    "setTribute(uint256)"
      :params    [tribute]
      :on-result [:tribute-to-talk.callback/set-tribute-transaction-sent]
      :on-error  [:tribute-to-talk.callback/set-tribute-transaction-failed]})
    {:db (assoc-in db
                   [:navigation/screen-params :tribute-to-talk :state]
                   :transaction-failed)}))

(fx/defn remove
  {:events [:tribute-to-talk.ui/remove-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            {:db (assoc-in db [:navigation/screen-params :tribute-to-talk]
                           {:step :finish
                            :state :disabled})}
            (set-tribute-signing-flow 0)))

(fx/defn set-step-finish
  [{:keys [db] :as cofx}]
  (let [tribute (get-in db [:navigation/screen-params :tribute-to-talk :snt-amount])]
    (fx/merge cofx
              {:db (assoc-in db [:navigation/screen-params :tribute-to-talk :state] :signing)}
              (set-tribute-signing-flow (tribute-to-talk.db/to-wei tribute))
              (set-step :finish))))

(fx/defn open-learn-more
  {:events [:tribute-to-talk.ui/learn-more-pressed]}
  [cofx]
  (set-step cofx :learn-more))

(fx/defn step-back
  {:events [:tribute-to-talk.ui/step-back-pressed]}
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

      :finish
      (set-step cofx :set-snt-amount))))

(fx/defn step-forward
  {:events [:tribute-to-talk.ui/step-forward-pressed]}
  [cofx]
  (let [{:keys [step editing?]}
        (get-in cofx [:db :navigation/screen-params :tribute-to-talk])]
    (case step
      :intro
      (set-step cofx :set-snt-amount)

      :set-snt-amount
      (set-step-finish cofx)

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
        ;; Disallow values larger or equal to 1 million
        (>= (js/parseFloat (str snt-amount numpad-symbol))
            tribute-to-talk.db/max-snt-amount)
        snt-amount
        ;; Replace initial "0" by the first digit
        (and (= snt-amount "0") (not= numpad-symbol "."))
        (str numpad-symbol)
        :else (str snt-amount numpad-symbol)))))

(fx/defn update-snt-amount
  {:events [:tribute-to-talk.ui/numpad-key-pressed]}
  [{:keys [db]} numpad-symbol]
  {:db (update-in db
                  [:navigation/screen-params :tribute-to-talk :snt-amount]
                  #(get-new-snt-amount % numpad-symbol))})

(fx/defn start-editing
  {:events [:tribute-to-talk.ui/edit-pressed]}
  [{:keys [db]}]
  {:db (assoc-in db
                 [:navigation/screen-params :tribute-to-talk :step]
                 :set-snt-amount)})

(fx/defn on-check-tribute-success
  {:events [:tribute-to-talk.callback/check-tribute-success]}
  [cofx public-key tribute-to-talk]
  (let [tribute-to-talk (when (tribute-to-talk.db/valid? tribute-to-talk)
                          tribute-to-talk)]
    (if-let [me? (= public-key
                    (get-in cofx [:db :account/account :public-key]))]
      (update-settings cofx tribute-to-talk)
      (contact/set-tribute cofx public-key tribute-to-talk))))

(fx/defn on-no-tribute-found
  {:events [:tribute-to-talk.callback/no-tribute-found]}
  [cofx public-key]
  (if-let [me? (= public-key
                  (get-in cofx [:db :account/account :public-key]))]
    (update-settings cofx nil)
    (contact/set-tribute cofx public-key nil)))

(re-frame/reg-fx
 :tribute-to-talk/get-tribute
 (fn [{:keys [contract address on-success]}]
   (json-rpc/eth-call
    {:contract contract
     :method "getTribute(address)"
     :params [address]
     :outputs ["uint256"]
     :on-success on-success})))

(fx/defn check-tribute
  [{:keys [db] :as cofx} public-key]
  (when (and (not (get-in db [:chats public-key :group-chat]))
             (not (get-in db [:contacts/contacts public-key :tribute-to-talk
                              :transaction-hash]))
             (not (whitelist/whitelisted-by?
                   (get-in db [:contacts/contacts public-key]))))
    (if-let [contract (contracts/get-address db :status/tribute-to-talk)]
      (let [address (ethereum/public-key->address public-key)]
        {:tribute-to-talk/get-tribute
         {:contract contract
          :address  address
          :on-success
          (fn [[tribute]]
            (re-frame/dispatch
             (if (pos? tribute)
               [:tribute-to-talk.callback/check-tribute-success
                public-key
                {:snt-amount (str tribute)}]
               [:tribute-to-talk.callback/no-tribute-found public-key])))}})
      ;; update settings if checking own manifest or do nothing otherwise
      (if-let [me? (= public-key
                      (get-in cofx [:db :account/account :public-key]))]

        (fx/merge cofx
                  {:db (assoc-in db
                                 [:navigation/screen-params :tribute-to-talk :unavailable?]
                                 true)}
                  (update-settings nil))
        (contact/set-tribute cofx public-key nil)))))

(fx/defn check-own-tribute
  [cofx]
  (check-tribute cofx (get-in cofx [:db :account/account :public-key])))

(fx/defn pay-tribute
  {:events [:tribute-to-talk.ui/on-pay-to-chat-pressed]}
  [{:keys [db] :as cofx} public-key]
  (let [{:keys [address public-key tribute-to-talk]}
        (get-in db [:contacts/contacts public-key])
        {:keys [snt-amount]} tribute-to-talk]
    (signing/eth-transaction-call
     cofx
     {:contract  (contracts/get-address db :status/snt)
      :method    "transfer(address,uint256)"
      :params    [address snt-amount]
      :on-result [:tribute-to-talk.callback/pay-tribute-transaction-sent public-key]})))

(defn tribute-transaction-trigger
  [db {:keys [block error?]}]
  (let [current-block (get db :ethereum/current-block)
        transaction-block (or block
                              current-block)]
    (or error?
        (pos? (- current-block
                 (js/parseInt transaction-block))))))

(fx/defn on-pay-tribute-transaction-triggered
  [{:keys [db] :as cofx}
   public-key
   {:keys [error? transfer symbol] :as transaction}]
  (when (and transfer
             (= symbol (ethereum/snt-symbol db))
             (not error?))
    (whitelist/mark-tribute-paid cofx public-key)))

(fx/defn on-pay-tribute-transaction-sent
  {:events [:tribute-to-talk.callback/pay-tribute-transaction-sent]}
  [{:keys [db] :as cofx} public-key transaction-hash]
  (fx/merge cofx
            {:db (assoc-in db [:contacts/contacts public-key
                               :tribute-to-talk :transaction-hash]
                           transaction-hash)}
            (transactions/watch-transaction
             transaction-hash
             {:trigger-fn
              tribute-transaction-trigger
              :on-trigger
              #(on-pay-tribute-transaction-triggered public-key %)})))

(fx/defn on-set-tribute-transaction-triggered
  [{:keys [db] :as cofx}
   tribute
   {:keys [error?] :as transaction}]
  (if error?
    (fx/merge cofx
              {:db (assoc-in db [:navigation/screen-params
                                 :tribute-to-talk :state]
                             :transaction-failed)}
              (update-settings {:update nil}))
    (fx/merge cofx
              {:db (assoc-in db [:navigation/screen-params
                                 :tribute-to-talk :state]
                             (if tribute
                               :completed
                               :disabled))}
              (check-own-tribute)
              (update-settings {:update nil}))))

(fx/defn on-set-tribute-transaction-sent
  {:events [:tribute-to-talk.callback/set-tribute-transaction-sent]}
  [{:keys [db] :as cofx} transaction-hash]
  (let [{:keys [snt-amount message]} (get-in db [:navigation/screen-params
                                                 :tribute-to-talk])]
    (fx/merge cofx
              {:db (assoc-in db [:navigation/screen-params
                                 :tribute-to-talk :state]
                             :pending)}
              (update-settings {:update {:transaction transaction-hash
                                         :snt-amount  snt-amount
                                         :message     message}})
              (transactions/watch-transaction
               transaction-hash
               {:trigger-fn
                tribute-transaction-trigger
                :on-trigger
                #(on-set-tribute-transaction-triggered snt-amount %)}))))

(fx/defn on-set-tribute-transaction-failed
  {:events [:tribute-to-talk.callback/set-tribute-transaction-failed]}
  [{:keys [db] :as cofx} error]
  (log/error :set-tribute-transaction-failed error)
  {:db (assoc-in db
                 [:navigation/screen-params :tribute-to-talk :state]
                 :transaction-failed)})

(fx/defn watch-set-tribute-transaction
  "check if there is a pending transaction to set the tribute and
   add a watch on that transaction
   if there is a transaction check if the trigger is valid already"
  [{:keys [db] :as cofx}]
  (when-let [tribute-update (get (tribute-to-talk.db/get-settings db)
                                 :update)]
    (let [{:keys [transaction snt-amount]} tribute-update]
      (fx/merge cofx
                (transactions/watch-transaction
                 transaction
                 {:trigger-fn
                  tribute-transaction-trigger
                  :on-trigger
                  #(on-set-tribute-transaction-triggered snt-amount %)})
                (when-let [transaction (get-in db [:wallet :transactions
                                                   transaction])]
                  (transactions/check-transaction transaction))))))

(fx/defn init
  [cofx]
  (fx/merge cofx
            (check-own-tribute)
            (watch-set-tribute-transaction)))
