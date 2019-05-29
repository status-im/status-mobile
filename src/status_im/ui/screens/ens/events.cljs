(ns status-im.ui.screens.ens.events
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.accounts.update.core :as update]
            [status-im.ethereum.abi-spec :as abi-spec]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.resolver :as resolver]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.ui.screens.navigation :as navigation]
            [status-im.utils.fx :as fx]
            [status-im.utils.handlers :as handlers]
            [status-im.utils.money :as money]
            [status-im.wallet.core :as wallet]))

(re-frame/reg-fx
 ::ens/resolve
 (fn [[registry name cb]]
   (ens/get-addr registry name cb)))

(defn- on-resolve [registry custom-domain? username address public-key s]
  (cond
    (= (ethereum/normalized-address address) (ethereum/normalized-address s))
    (resolver/pubkey registry username
                     (fn [ss]
                       (if (= ss public-key)
                         (re-frame/dispatch [:ens/set-state :connected])
                         (re-frame/dispatch [:ens/set-state :owned]))))

    (and (nil? s) (not custom-domain?)) ;; No address for a stateofus subdomain: it can be registered
    (re-frame/dispatch [:ens/set-state :registrable])

    :else
    (re-frame/dispatch [:ens/set-state :unregistrable])))

(defn- assoc-state [db state]
  (assoc-in db [:ens :state] state))

(defn- assoc-username [db username]
  (assoc-in db [:ens :username] username))

(defn- valid-custom-domain? [username]
  (and (ens/is-valid-eth-name? username)
       (stateofus/lower-case? username)))

(defn- valid-username? [custom-domain? username]
  (if custom-domain?
    (valid-custom-domain? username)
    (stateofus/valid-username? username)))

(handlers/register-handler-fx
 :ens/set-state
 (fn [{:keys [db]} [_ state]]
   {:db (assoc-state db state)}))

(defn- state [valid? username]
  (cond
    (string/blank? username) :initial
    valid? :typing
    :else
    :invalid))

(handlers/register-handler-fx
 :ens/set-username
 (fn [{:keys [db]} [_ custom-domain? username]]
   (let [valid? (valid-username? custom-domain? username)]
     (merge
      {:db (-> db
               (assoc-username username)
               (assoc-state (state valid? username)))}
      (when valid?
        (let [{:keys [account/account]}        db
              {:keys [address public-key]}     account
              registry (get ens/ens-registries (ethereum/chain-keyword db))
              name     (if custom-domain? username (stateofus/subdomain username))]
          {::ens/resolve [registry name #(on-resolve registry custom-domain? name address public-key %)]}))))))

(handlers/register-handler-fx
 :ens/navigate-back
 (fn [{:keys [db] :as cofx} _]
   (fx/merge cofx
             {:db (-> db
                      (assoc-state :initial)
                      (assoc-username ""))}
             (navigation/navigate-back))))

(handlers/register-handler-fx
 :ens/switch-domain-type
 (fn [{:keys [db]} _]
   {:db (-> (update-in db [:ens :custom-domain?] not)
            (assoc-state :initial)
            (assoc-username ""))}))

(handlers/register-handler-fx
 :ens/save-username
 (fn [{:keys [db] :as cofx} [_ username]]
   (let [db (update-in db [:account/account :usernames] #((fnil conj []) %1 %2) username)]
     (fx/merge
      cofx
      {:db       db
       :dispatch [:navigate-back]}
      (update/account-update {:usernames (get-in db [:account/account :usernames])}
                             {:success-event [:ens/set-state :saved]})))))

(handlers/register-handler-fx
 :ens/on-registration-failure
 (fn [{:keys [db]} _]
   {:db (assoc-state db :registration-failed)}))

(handlers/register-handler-fx
 :ens/register
 (fn [cofx [_ {:keys [contract username address public-key]}]]
   (let [{:keys [x y]} (ethereum/coordinates public-key)]
     (wallet/eth-transaction-call
      cofx
      {:contract   "0x744d70fdbe2ba4cf95131626614a1763df805b9e"
       :method     "approveAndCall(address,uint256,bytes)"
       :params     [contract
                    (money/unit->token 10 18)
                    (abi-spec/encode "register(bytes32,address,bytes32,bytes32)"
                                     [(ethereum/sha3 username) address x y])]
       :to-name    "Stateofus registrar"
       :amount     (money/bignumber 0)
       :gas        (money/bignumber 200000)
       :on-result  [:ens/save-username username]
       :on-error   [:ens/on-registration-failure]}))))
