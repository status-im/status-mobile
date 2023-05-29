(ns status-im.ens.core
  (:refer-clojure :exclude [name])
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.bottom-sheet.events :as bottom-sheet]
            [status-im.ethereum.core :as ethereum]
            [status-im.ethereum.eip55 :as eip55]
            [status-im.ethereum.ens :as ens]
            [status-im.ethereum.stateofus :as stateofus]
            [status-im.multiaccounts.update.core :as multiaccounts.update]
            [utils.re-frame :as rf]
            [utils.datetime :as datetime]
            [status-im.utils.random :as random]
            [status-im2.navigation.events :as navigation]))

(defn fullname
  [custom-domain? username]
  (if custom-domain?
    username
    (stateofus/subdomain username)))

(re-frame/reg-fx
 ::resolve-address
 (fn [[chain-id name cb]]
   (ens/address chain-id name cb)))

(re-frame/reg-fx
 ::resolve-owner
 (fn [[chain-id name cb]]
   (ens/owner chain-id name cb)))

(re-frame/reg-fx
 ::resolve-pubkey
 (fn [[chain-id name cb]]
   (ens/pubkey chain-id name cb)))

(re-frame/reg-fx
 ::get-expiration-time
 (fn [[chain-id name cb]]
   (ens/expire-at chain-id name cb)))

(rf/defn update-ens-tx-state
  {:events [:update-ens-tx-state]}
  [{:keys [db]} new-state username custom-domain? tx-hash]
  {:db (assoc-in db
        [:ens/registrations tx-hash]
        {:state          new-state
         :username       username
         :custom-domain? custom-domain?})})

(rf/defn redirect-to-ens-summary
  {:events [::redirect-to-ens-summary]}
  [cofx]
  ;; we reset navigation so that navigate back doesn't return
  ;; into the registration flow
  (navigation/set-stack-root cofx
                             :profile-stack
                             [:my-profile
                              :ens-confirmation]))

(rf/defn update-ens-tx-state-and-redirect
  {:events [:update-ens-tx-state-and-redirect]}
  [cofx new-state username custom-domain? tx-hash]
  (rf/merge cofx
            (update-ens-tx-state new-state username custom-domain? tx-hash)
            (redirect-to-ens-summary)))

(rf/defn clear-ens-registration
  {:events [:clear-ens-registration]}
  [{:keys [db]} tx-hash]
  {:db (update db :ens/registrations dissoc tx-hash)})

(rf/defn set-state
  {:events [::name-resolved]}
  [{:keys [db]} username state address]
  (when (= username
           (get-in db [:ens/registration :username]))
    {:db (-> db
             (assoc-in [:ens/registration :state] state)
             (assoc-in [:ens/registration :address] address))}))

(rf/defn save-username
  {:events [::save-username]}
  [{:keys [db] :as cofx} custom-domain? username redirectToSummary]
  (let [name      (fullname custom-domain? username)
        names     (get-in db [:multiaccount :usernames] [])
        new-names (conj names name)]
    (rf/merge cofx
              (multiaccounts.update/multiaccount-update
               :usernames
               new-names
               (when redirectToSummary
                 {:on-success #(re-frame/dispatch [::redirect-to-ens-summary])}))
              (when (empty? names)
                (multiaccounts.update/multiaccount-update
                 :preferred-name
                 name
                 {})))))

(rf/defn set-pub-key
  {:events [::set-pub-key]}
  [{:keys [db]}]
  (let [{:keys [username address custom-domain?]} (:ens/registration db)
        address                                   (or address (ethereum/default-address db))
        {:keys [public-key]}                      (:multiaccount db)
        chain-id                                  (ethereum/chain-id db)
        username                                  (fullname custom-domain? username)]
    (ens/set-pub-key-prepare-tx
     chain-id
     address
     username
     public-key
     #(re-frame/dispatch [:signing.ui/sign
                          {:tx-obj    %
                           :on-result [::save-username custom-domain? username true]
                           :on-error  [::on-registration-failure]}]))))

(rf/defn on-input-submitted
  {:events [::input-submitted]}
  [{:keys [db] :as cofx}]
  (let [{:keys [state username custom-domain?]} (:ens/registration db)]
    (case state
      (:available :owned)
      (navigation/navigate-to cofx :ens-checkout {})
      :connected-with-different-key
      (re-frame/dispatch [::set-pub-key])
      :connected
      (save-username cofx custom-domain? username true)
      ;; for other states, we do nothing
      nil)))

(defn- on-resolve-owner
  [chain-id custom-domain? username addresses public-key response resolve-last-id* resolve-last-id]
  (when (= @resolve-last-id* resolve-last-id)
    (cond

      ;; No address for a stateofus subdomain: it can be registered
      (and (= response ens/default-address) (not custom-domain?))
      (re-frame/dispatch [::name-resolved username :available])

      ;; if we get an address back, we try to get the public key associated
      ;; with the username as well
      (get addresses (eip55/address->checksum response))
      (ens/pubkey
       chain-id
       (fullname custom-domain? username)
       #(re-frame/dispatch
         [::name-resolved username
          (cond
            (not public-key) :owned
            (and (string/ends-with? % "0000000000000000000000000000000000000000000000000000000000000000")
                 (not custom-domain?))
            :invalid-ens
            (= % public-key) :connected
            :else :connected-with-different-key)
          (eip55/address->checksum response)]))
      :else
      (re-frame/dispatch [::name-resolved username :taken]))))

(defn registration-cost
  [chain-id]
  (case chain-id
    3 50
    5 10
    1 10))

(rf/defn register-name
  {:events [::register-name-pressed]}
  [{:keys [db]} address]
  (let [{:keys [username]}
        (:ens/registration db)
        {:keys [public-key]} (:multiaccount db)
        chain-id (ethereum/chain-id db)]
    (ens/register-prepare-tx
     chain-id
     address
     username
     public-key
     #(re-frame/dispatch [:signing.ui/sign
                          {:tx-obj    %
                           :on-result [:update-ens-tx-state-and-redirect :submitted username false]
                           :on-error  [::on-registration-failure]}]))))

(defn- valid-custom-domain?
  [username]
  (and (ens/is-valid-eth-name? username)
       (stateofus/lower-case? username)))

(defn- valid-username?
  [custom-domain? username]
  (if custom-domain?
    (valid-custom-domain? username)
    (stateofus/valid-username? username)))

(defn- state
  [custom-domain? username usernames]
  (cond
    (or (string/blank? username)
        (> 4 (count username)))
    :too-short
    (valid-username? custom-domain? username)
    (if (usernames (fullname custom-domain? username))
      :already-added
      :searching)
    :else :invalid))

;;NOTE we want to handle only last resolve
(def resolve-last-id (atom nil))

(rf/defn set-username-candidate
  {:events [::set-username-candidate]}
  [{:keys [db]} username]
  (let [{:keys [custom-domain?]} (:ens/registration db)
        usernames                (into #{} (get-in db [:multiaccount :usernames]))
        state                    (state custom-domain? username usernames)]
    (reset! resolve-last-id (random/id))
    (merge
     {:db (update db
                  :ens/registration assoc
                  :username         username
                  :state            state)}
     (when (= state :searching)
       (let [{:keys [multiaccount]} db
             {:keys [public-key]}   multiaccount
             addresses              (ethereum/addresses-without-watch db)
             chain-id               (ethereum/chain-id db)]
         {::resolve-owner [chain-id
                           (fullname custom-domain? username)
                           #(on-resolve-owner
                             chain-id
                             custom-domain?
                             username
                             addresses
                             public-key
                             %
                             resolve-last-id
                             @resolve-last-id)]})))))

(rf/defn return-to-ens-main-screen
  {:events [::got-it-pressed ::cancel-pressed]}
  [{:keys [db] :as cofx} _]
  (rf/merge cofx
            ;; clear registration data
            {:db (dissoc db :ens/registration)}
            ;; we reset navigation so that navigate back doesn't return
            ;; into the registration flow
            (navigation/set-stack-root :profile-stack
                                       [:my-profile
                                        :ens-main])))

(rf/defn switch-domain-type
  {:events [::switch-domain-type]}
  [{:keys [db] :as cofx} _]
  (rf/merge cofx
            {:db (-> db
                     (update :ens/registration dissoc :username :state)
                     (update-in [:ens/registration :custom-domain?] not))}))

(rf/defn change-address
  {:events [::change-address]}
  [{:keys [db] :as cofx} _ {:keys [address]}]
  (rf/merge cofx
            {:db (assoc-in db [:ens/registration :address] address)}
            (bottom-sheet/hide-bottom-sheet-old)))

(rf/defn save-preferred-name
  {:events [::save-preferred-name]}
  [cofx name]
  (multiaccounts.update/multiaccount-update cofx
                                            :preferred-name
                                            name
                                            {}))

(rf/defn on-registration-failure
  "TODO not sure there is actually anything to do here
   it should only be called if the user cancels the signing
   Actual registration failure has not been implemented properly"
  {:events [::on-registration-failure]}
  [_ _])

(rf/defn store-name-address
  {:events [::address-resolved]}
  [{:keys [db]} username address]
  {:db (assoc-in db [:ens/names username :address] address)})

(rf/defn store-name-public-key
  {:events [::public-key-resolved]}
  [{:keys [db]} username public-key]
  {:db (assoc-in db [:ens/names username :public-key] public-key)})

(rf/defn store-expiration-date
  {:events [::get-expiration-time-success]}
  [{:keys [now db]} username timestamp]
  {:db (-> db
           (assoc-in [:ens/names username :expiration-date]
                     (datetime/timestamp->year-month-day-date timestamp))
           (assoc-in [:ens/names username :releasable?] (<= timestamp now)))})

(rf/defn navigate-to-name
  {:events [::navigate-to-name]}
  [{:keys [db] :as cofx} username]
  (let [chain-id (ethereum/chain-id db)]
    (rf/merge cofx
              {::get-expiration-time
               [chain-id
                (stateofus/username username)
                #(re-frame/dispatch [::get-expiration-time-success username %])]
               ::resolve-address
               [chain-id username
                #(re-frame/dispatch [::address-resolved username %])]
               ::resolve-pubkey
               [chain-id username
                #(re-frame/dispatch [::public-key-resolved username %])]}
              (navigation/navigate-to :ens-name-details username))))

(rf/defn start-registration
  {:events [::add-username-pressed ::get-started-pressed]}
  [{:keys [db] :as cofx}]
  (rf/merge cofx
            (set-username-candidate (get-in db [:ens/registration :username] ""))
            (navigation/navigate-to :ens-search {})))

(rf/defn remove-username
  {:events [::remove-username]}
  [{:keys [db] :as cofx} name]
  (let [names          (get-in db [:multiaccount :usernames] [])
        preferred-name (get-in db [:multiaccount :preferred-name])
        new-names      (remove #(= name %) names)]
    (rf/merge cofx
              (multiaccounts.update/multiaccount-update
               :usernames
               new-names
               {})
              (when (= name preferred-name)
                (multiaccounts.update/multiaccount-update
                 :preferred-name
                 (first new-names)
                 {}))
              ((navigation/navigate-back nil)))))
