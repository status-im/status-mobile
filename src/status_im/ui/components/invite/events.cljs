(ns status-im.ui.components.invite.events
  (:require [re-frame.core :as re-frame]
            [reagent.ratom :refer [make-reaction]]
            [status-im.utils.fx :as fx]
            [status-im.ethereum.json-rpc :as json-rpc]
            [status-im.ethereum.contracts :as contracts]
            [status-im.ethereum.core :as ethereum]
            [status-im.ui.components.react :as react]
            [status-im.navigation :as navigation]
            [status-im.utils.universal-links.core :as universal-links]
            [status-im.acquisition.core :as acquisition]
            [status-im.utils.money :as money]))

(def privacy-policy-link "https://get.status.im")

(re-frame/reg-fx
 ::share
 (fn [content]
   (.share ^js react/sharing (clj->js content))))

(fx/defn share-link
  {:events [::share-link]}
  [{:keys [db]} response]
  (let [{:keys [public-key preferred-name]} (get db :multiaccount)
        invite-id                           (get response :invite_code)
        profile-link                        (universal-links/generate-link :user :external
                                                                           (or preferred-name public-key))
        share-link                          (cond-> profile-link
                                              invite-id
                                              (str "?invite=" invite-id))
        message                             (str "Hey join me on Status: " share-link)]
    {::share {:message message}}))

(fx/defn generate-invite
  {:events [::generate-invite]}
  [{:keys [db] :as cofx} {:keys [address]}]
  (acquisition/handle-registration cofx
                                   {:message    {:address             address
                                                 :interaction_address (get-in db [:multiaccount :public-key])}
                                    :on-success ::share-link}))

(re-frame/reg-sub
 ::pending-chat-invite
 (fn [db]
   (let [chat-id               (get db :current-chat-id)
         {:keys [flow-state]}  (get db :acquisition)
         {:keys [attributed]
          :as   chat-referrer} (get-in db [:acquisition :chat-referrer chat-id])]
     (and chat-referrer
          (not attributed)
          (nil? flow-state)))))

(fx/defn go-to-invite
  {:events [::open-invite]}
  [{:keys [db] :as cofx}]
  (let [contract (contracts/get-address db :status/acquisition)
        accounts (filter #(not= (:type %) :watch) (get db :multiaccount/accounts))]
    (fx/merge cofx
              {::get-rewards (mapv (fn [{:keys [address]}]
                                     {:address    address
                                      :contract   contract
                                      :on-success (fn [type data]
                                                    (re-frame/dispatch [::get-reward-success address type data]))})
                                   accounts)}
              (navigation/navigate-to-cofx :referral-invite nil))))

(re-frame/reg-fx
 ::terms-and-conditions
 (fn []
   (.openURL ^js react/linking privacy-policy-link)))

(fx/defn open-privacy-policy-link
  {:events [::terms-and-conditions]}
  [_]
  {::terms-and-conditions nil})

;; Invite reward


(re-frame/reg-sub
 :invite/accounts-reward
 (fn [db]
   (get-in db [:acquisition :accounts])))

(re-frame/reg-sub
 :invite/account-reward
 :<- [:invite/accounts-reward]
 (fn [accounts [_ account]]
   (get accounts account)))

(defn- get-reward [contract address on-success]
  (json-rpc/eth-call
   {:contract   contract
    :method     "getReferralReward(address,bool)"
    :params     [address false]
    ;; [uint ethAmount, uint tokenLen, uint maxThreshold, uint attribCount]
    :outputs    ["uint256" "uint256" "uint256" "uint256"]
    :on-success (fn [[eth-amount tokens-count max-threshold attrib-count]]
                  (on-success :reward [eth-amount tokens-count max-threshold attrib-count])
                  (dotimes [id tokens-count]
                    (json-rpc/eth-call
                     {:contract   contract
                      :method     "getReferralRewardTokens(address,bool,uint256)"
                      :params     [address false id]
                      :outputs    ["address" "uint256"]
                      :on-success (fn [token-data]
                                    (on-success :token token-data))})))}))

(re-frame/reg-fx
 ::get-rewards
 (fn [accounts]
   (doseq [{:keys [contract address on-success]} accounts]
     (get-reward contract address on-success))))

(fx/defn default-reward-success
  {:events [::default-reward-success]}
  [{:keys [db]} type data]
  (if (= :reward type)
    (let [[eth-amount tokens-count max-threshold attrib-count] data]
      {:db (assoc-in db [:acquisition :referral-reward] {:eth-amount    (money/wei->ether eth-amount)
                                                         :tokens-count  tokens-count
                                                         :max-threshold max-threshold
                                                         :attrib-count  attrib-count})})
    (let [[address amount] data]
      {:db (assoc-in db [:acquisition :referral-reward :tokens address] (money/wei->ether amount))})))

(fx/defn get-reward-success
  {:events [::get-reward-success]}
  [{:keys [db]} account type data]
  (if (= :reward type)
    (let [[eth-amount _ max-threshold attrib-count] data]
      {:db (assoc-in db [:acquisition :accounts account] {:eth-amount    (money/wei->ether eth-amount)
                                                          :max-threshold max-threshold
                                                          :attrib-count  attrib-count})})
    (let [[address amount] data]
      {:db (assoc-in db [:acquisition :accounts account :tokens address] (money/wei->ether amount))})))

(fx/defn get-default-reward
  {:events [::get-default-reward]}
  [{:keys [db]}]
  {::get-rewards [{:contract   (contracts/get-address db :status/acquisition)
                   :address    (ethereum/default-address db)
                   :on-success (fn [type data]
                                 (re-frame/dispatch [::default-reward-success type data]))}]})

(re-frame/reg-sub-raw
 ::default-reward
 (fn [db]
   (re-frame/dispatch [::get-default-reward])
   (make-reaction
    (fn []
      (get-in @db [:acquisition :referral-reward])))))

;; Starter pack

(fx/defn get-starter-pack-amount
  {:events [::starter-pack-amount]}
  [{:keys [db]} [_ eth-amount tokens tokens-amount sticker-packs]]
  {:db (assoc-in db [:acquisition :starter-pack :pack]
                 {:eth-amount    (money/wei->ether eth-amount)
                  :tokens        (zipmap tokens
                                         (map money/wei->ether tokens-amount))
                  :sticker-packs sticker-packs})})

(fx/defn starter-pack
  {:events [::starter-pack]}
  [{:keys [db]}]
  (let [contract (contracts/get-address db :status/acquisition)]
    {::json-rpc/eth-call [{:contract   contract
                           :method     "getDefaultPack()"
                           :outputs    ["address" "uint256" "address[]" "uint256[]" "uint256[]"]
                           :on-success #(re-frame/dispatch [::starter-pack-amount (vec %) (prn %)])}]}))

(re-frame/reg-sub-raw
 ::starter-pack
 (fn [db]
   (re-frame/dispatch [::starter-pack])
   (make-reaction
    (fn []
      (get-in @db [:acquisition :starter-pack :pack])))))
