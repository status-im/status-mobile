(ns status-im.ens.core
  (:refer-clojure :exclude [name])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.ethereum.abi-spec :as abi-spec]
            [status-im.ethereum.contracts :as contracts]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.resolver :as resolver]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [status-im.signing.core :as signing]
            [status-im.navigation :as navigation]
            [status-im.utils.datetime :as datetime]
            [status-im.utils.fx :as fx]
            [status-im.utils.money :as money]
            [status-im.utils.random :as random]))

(defn fullname [custom-domain? username]
  (if custom-domain?
    username
    (stateofus/subdomain username)))

(fx/defn update-ens-tx-state
  {:events [:update-ens-tx-state]}
  [{:keys [db]} new-state username custom-domain? tx-hash]
  {:db (assoc-in db [:ens/registrations tx-hash] {:state          new-state
                                                  :username       username
                                                  :custom-domain? custom-domain?})})

(fx/defn redirect-to-ens-summary
  {:events [::redirect-to-ens-summary]}
  [cofx]
  ;; we reset navigation so that navigate back doesn't return
  ;; into the registration flow
  (navigation/set-stack-root cofx
                             :profile-stack
                             [:my-profile
                              :ens-confirmation]))

(fx/defn update-ens-tx-state-and-redirect
  {:events [:update-ens-tx-state-and-redirect]}
  [cofx new-state username custom-domain? tx-hash]
  (fx/merge cofx
            (update-ens-tx-state new-state username custom-domain? tx-hash)
            (redirect-to-ens-summary)))

(fx/defn clear-ens-registration
  {:events [:clear-ens-registration]}
  [{:keys [db]} tx-hash]
  {:db (update db :ens/registrations dissoc tx-hash)})

(re-frame/reg-fx
 ::resolve-address
 (fn [[registry name cb]]
   (ens/get-addr registry name cb)))

(re-frame/reg-fx
 ::resolve-owner
 (fn [[registry name cb]]
   (ens/get-owner registry name cb)))

(re-frame/reg-fx
 ::resolve-pubkey
 (fn [[registry name cb]]
   (resolver/pubkey registry name cb)))

(re-frame/reg-fx
 ::get-expiration-time
 (fn [[chain label-hash cb]]
   (stateofus/get-registrar
    chain
    (fn [registrar]
      (stateofus/get-expiration-time registrar label-hash cb)))))

(fx/defn set-state
  {:events [::name-resolved]}
  [{:keys [db]} username state]
  (when (= username
           (get-in db [:ens/registration :username]))
    {:db (assoc-in db [:ens/registration :state] state)}))

(fx/defn on-resolver-found
  {:events [::resolver-found]}
  [{:keys [db] :as cofx} resolver-contract]
  (let [{:keys [state username custom-domain?]} (:ens/registration db)
        {:keys [public-key]} (:multiaccount db)
        {:keys [x y]} (ethereum/coordinates public-key)
        namehash (ens/namehash (str username (when-not custom-domain?
                                               ".stateofus.eth")))]
    (signing/eth-transaction-call
     cofx
     {:contract   resolver-contract
      :method     "setPubkey(bytes32,bytes32,bytes32)"
      :params     [namehash x y]
      :on-result  [::save-username custom-domain? username true]
      :on-error   [::on-registration-failure]})))

(fx/defn save-username
  {:events [::save-username]}
  [{:keys [db] :as cofx} custom-domain? username redirectToSummary]
  (let [name   (fullname custom-domain? username)
        names  (get-in db [:multiaccount :usernames] [])
        new-names (conj names name)]
    (fx/merge cofx
              (multiaccounts.update/multiaccount-update
               :usernames new-names
               (when redirectToSummary
                 {:on-success #(re-frame/dispatch [::redirect-to-ens-summary])}))
              (when (empty? names)
                (multiaccounts.update/multiaccount-update
                 :preferred-name name {})))))

(fx/defn on-input-submitted
  {:events [::input-submitted ::input-icon-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [state username custom-domain?]} (:ens/registration db)
        registry-contract (get ens/ens-registries (ethereum/chain-keyword db))
        ens-name (str username (when-not custom-domain?
                                 ".stateofus.eth"))]
    (case state
      (:available :owned)
      (navigation/navigate-to-cofx cofx :ens-checkout {})
      :connected-with-different-key
      (ens/resolver registry-contract ens-name
                    #(re-frame/dispatch [::resolver-found %]))
      :connected
      (save-username cofx custom-domain? username true)
      ;; for other states, we do nothing
      nil)))

(defn- on-resolve-owner
  [registry custom-domain? username address public-key response resolve-last-id* resolve-last-id]
  (when (= @resolve-last-id* resolve-last-id)
    (cond

      ;; No address for a stateofus subdomain: it can be registered
      (and (= response ens/default-address) (not custom-domain?))
      (re-frame/dispatch [::name-resolved username :available])

      ;; if we get an address back, we try to get the public key associated
      ;; with the username as well
      (= (eip55/address->checksum address)
         (eip55/address->checksum response))
      (resolver/pubkey registry (fullname custom-domain? username)
                       #(re-frame/dispatch [::name-resolved username
                                            (cond
                                              (not public-key) :owned
                                              (= % public-key) :connected
                                              :else :connected-with-different-key)]))

      :else
      (re-frame/dispatch [::name-resolved username :taken]))))

(defn registration-cost
  [chain-id]
  (case chain-id
    3 50
    1 10))

(fx/defn register-name
  {:events [::register-name-pressed]}
  [{:keys [db] :as cofx}]
  (let [{:keys [custom-domain? username]}
        (:ens/registration db)
        {:keys [public-key]} (:multiaccount db)
        address (ethereum/default-address db)
        chain (ethereum/chain-keyword db)
        chain-id (ethereum/chain-id db)
        amount (registration-cost chain-id)
        {:keys [x y]} (ethereum/coordinates public-key)]
    (stateofus/get-registrar
     chain
     (fn [contract]
       (signing/eth-transaction-call
        cofx
        {:contract   (contracts/get-address db :status/snt)
         :method     "approveAndCall(address,uint256,bytes)"
         :params     [contract
                      (money/unit->token amount 18)
                      (abi-spec/encode "register(bytes32,address,bytes32,bytes32)"
                                       [(ethereum/sha3 username) address x y])]
         :on-result  [:update-ens-tx-state-and-redirect :submitted username custom-domain?]
         :on-error   [::on-registration-failure]})))))

(defn- valid-custom-domain? [username]
  (and (ens/is-valid-eth-name? username)
       (stateofus/lower-case? username)))

(defn- valid-username? [custom-domain? username]
  (if custom-domain?
    (valid-custom-domain? username)
    (stateofus/valid-username? username)))

(defn- state [custom-domain? username usernames]
  (cond
    (or (string/blank? username)
        (> 4 (count username))) :too-short
    (valid-username? custom-domain? username)
    (if (usernames (fullname custom-domain? username))
      :already-added
      :searching)
    :else :invalid))

;;NOTE we want to handle only last resolve
(def resolve-last-id (atom nil))

(fx/defn set-username-candidate
  {:events [::set-username-candidate]}
  [{:keys [db]} username]
  (let [{:keys [custom-domain?]} (:ens/registration db)
        usernames (into #{} (get-in db [:multiaccount :usernames]))
        state (state custom-domain? username usernames)]
    (reset! resolve-last-id (random/id))
    (merge
     {:db (update db :ens/registration assoc
                  :username username
                  :state state)}
     (when (= state :searching)
       (let [{:keys [multiaccount]} db
             {:keys [public-key]} multiaccount
             address (ethereum/default-address db)
             registry (get ens/ens-registries (ethereum/chain-keyword db))]
         {::resolve-owner [registry
                           (fullname custom-domain? username)
                           #(on-resolve-owner
                             registry custom-domain? username address public-key %
                             resolve-last-id @resolve-last-id)]})))))

(fx/defn return-to-ens-main-screen
  {:events [::got-it-pressed ::cancel-pressed]}
  [{:keys [db] :as cofx} _]
  (fx/merge cofx
            ;; clear registration data
            {:db (dissoc db :ens/registration)}
            ;; we reset navigation so that navigate back doesn't return
            ;; into the registration flow
            (navigation/set-stack-root :profile-stack [:my-profile
                                                       :ens-main])))

(fx/defn switch-domain-type
  {:events [::switch-domain-type]}
  [{:keys [db] :as cofx} _]
  (fx/merge cofx
            {:db (-> db
                     (update :ens/registration dissoc :username :state)
                     (update-in [:ens/registration :custom-domain?] not))}))

(fx/defn save-preferred-name
  {:events [::save-preferred-name]}
  [{:keys [db] :as cofx} name]
  (multiaccounts.update/multiaccount-update cofx
                                            :preferred-name name
                                            {}))

(fx/defn on-registration-failure
  "TODO not sure there is actually anything to do here
   it should only be called if the user cancels the signing
   Actual registration failure has not been implemented properly"
  {:events [::on-registration-failure]}
  [{:keys [db]} username])

(fx/defn store-name-address
  {:events [::address-resolved]}
  [{:keys [db]} username address]
  {:db (assoc-in db [:ens/names username :address] address)})

(fx/defn store-name-public-key
  {:events [::public-key-resolved]}
  [{:keys [db]} username public-key]
  {:db (assoc-in db [:ens/names username :public-key] public-key)})

(fx/defn store-expiration-date
  {:events [::get-expiration-time-success]}
  [{:keys [now db]} username timestamp]
  {:db (-> db
           (assoc-in [:ens/names username :expiration-date]
                     (datetime/timestamp->year-month-day-date timestamp))
           (assoc-in [:ens/names username :releasable?] (<= timestamp now)))})

(fx/defn navigate-to-name
  {:events [::navigate-to-name]}
  [{:keys [db] :as cofx} username]
  (let [chain (ethereum/chain-keyword db)
        registry (get ens/ens-registries chain)]
    (fx/merge cofx
              {::get-expiration-time
               [chain
                (-> username
                    stateofus/username
                    ethereum/sha3)
                #(re-frame/dispatch [::get-expiration-time-success username %])]
               ::resolve-address
               [registry username
                #(re-frame/dispatch [::address-resolved username %])]
               ::resolve-pubkey
               [registry username
                #(re-frame/dispatch [::public-key-resolved username %])]}
              (navigation/navigate-to-cofx :ens-name-details username))))

(fx/defn start-registration
  {:events [::add-username-pressed ::get-started-pressed]}
  [{:keys [db] :as cofx}]
  (fx/merge cofx
            (set-username-candidate (get-in db [:ens/registration :username] ""))
            (navigation/navigate-to-cofx :ens-search {})))

(fx/defn remove-username
  {:events [::remove-username]}
  [{:keys [db] :as cofx} name]
  (let [names          (get-in db [:multiaccount :usernames] [])
        preferred-name (get-in db [:multiaccount :preferred-name])
        new-names      (remove #(= name %) names)]
    (fx/merge cofx
              (multiaccounts.update/multiaccount-update
               :usernames new-names
               {})
              (when (= name preferred-name)
                (multiaccounts.update/multiaccount-update
                 :preferred-name (first new-names) {}))
              (navigation/navigate-back))))
