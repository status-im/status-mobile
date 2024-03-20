(ns legacy.status-im.communities.e2e
  (:require [promesa.core :as p]
            [status-im.common.json-rpc.events :as rpc]
            [status-im.constants :as constants]
            [taoensso.timbre :as log]
            [utils.re-frame :as rf]))

;;NOTE: ONLY FOR QA

(def one-stt-in-wei "1000000000000000000")
(def one-eth-in-wei "1000000000000000000")
(def ten-stt-in-wei "10000000000000000000")

(def stt-symbol "STT")
(def eth-symbol "ETH")
(def null-address "0x0000000000000000000000000000000000000000")

(defn eth-token-criteria
  [amount-in-wei]
  {:contractAddresses {constants/ethereum-sepolia-chain-id null-address
                       constants/arbitrum-sepolia-chain-id null-address
                       constants/optimism-sepolia-chain-id null-address}
   :type              constants/community-token-type-erc20
   :symbol            "ETH"
   :name              "Ethereum"
   :amountInWei       amount-in-wei
   :decimals          18})

(defn stt-token-criteria
  [amount-in-wei]
  {:contractAddresses {constants/ethereum-sepolia-chain-id constants/sepolia-stt-contract-address}
   :type              constants/community-token-type-erc20
   :symbol            "STT"
   :name              "Status Test Token"
   :amountInWei       amount-in-wei
   :decimals          18})

(def token-criteria
  {stt-symbol stt-token-criteria
   eth-symbol eth-token-criteria})

(def base-channel-permissions
  [{:permission-type constants/community-token-permission-can-view-channel
    :token-symbol    stt-symbol
    :amount          one-stt-in-wei}
   {:permission-type constants/community-token-permission-can-view-channel
    :token-symbol    eth-symbol
    :amount          one-eth-in-wei}
   {:permission-type constants/community-token-permission-can-view-and-post-channel
    :token-symbol    stt-symbol
    :amount          one-stt-in-wei}
   {:permission-type constants/community-token-permission-can-view-and-post-channel
    :token-symbol    eth-symbol
    :amount          one-eth-in-wei}])

(defn channel-names->description
  [channel-names]
  (map #(hash-map :name        %1
                  :permissions [%2])
       channel-names
       base-channel-permissions))

(def community-descriptions
  {:token-gated
   {:community-name        "Token gated community 25"
    :community-permissions [{:amount-in-wei   ten-stt-in-wei
                             :permission-type constants/community-token-permission-become-member
                             :token-symbol    stt-symbol}]
    :channel-list          (channel-names->description ["Lions" "Birds" "Trees" "Flowers"])}
   :snt-admin
   {:community-name        "SNT Admin Community"
    :community-permissions [{:amount-in-wei   one-stt-in-wei
                             :permission-type constants/community-token-permission-become-admin
                             :token-symbol    stt-symbol}]
    :channel-list          (channel-names->description ["Sounds" "Colors" "Books" "Sports"])}
   :admin-and-member
   {:community-name        "Admin and Member"
    :community-permissions [{:amount-in-wei   one-stt-in-wei
                             :permission-type constants/community-token-permission-become-member
                             :token-symbol    stt-symbol}
                            {:amount-in-wei   one-eth-in-wei
                             :permission-type constants/community-token-permission-become-admin
                             :token-symbol    eth-symbol}]
    :channel-list          (channel-names->description ["Party" "Birthday" "Travel" "Cars"])}})


(defn js-response->community-id
  [response]
  (-> response .-communities first .-id))

(defn js-response->channel-id
  [channel-name response]
  (->> response
       .-communities
       first
       .-chats
       js->clj
       vals
       (some #(when (= channel-name (get % "name")) (get % "id")))))

(defn response->community-id-fn
  [callback]
  (comp callback js-response->community-id))

(defn channel-token-criteria->request
  [community-id channel-id {:keys [token-symbol amount]}]
  (fn []
    (rpc/call
     {:method      "wakuext_createCommunityTokenPermission2"
      :js-response true
      :params      [{:communityID   community-id
                     :type          constants/community-token-permission-become-member
                     :chatIds       [channel-id]
                     :tokenCriteria [((token-criteria token-symbol) amount)]}]})))

(defn channel-token-criteria->requests
  [community-id channel-id tks]
  (let [keep-fn (partial channel-token-criteria->request community-id channel-id)]
    (keep keep-fn tks)))

(defn create-community-channel!
  [channel community-id]
  (->
    (rpc/call
     {:method      "wakuext_createCommunityChannel"
      :js-response true
      :params      [{:name        (:name channel)
                     :communityId community-id
                     :description (:name channel)}]
      :on-error    #(log/error "failed to create token gated community channel."
                               {:error        %
                                :channel-name (:name channel)})})
    (.then (fn [response]
             (let [channel-id (js-response->channel-id (:name channel) response)]
               (apply
                p/chain
                (p/resolved nil)
                (channel-token-criteria->requests community-id channel-id (:permissions channel))))))))

(defn create-community!
  [community-name]
  (rpc/call
   {:method      "wakuext_createCommunity"
    :js-response true
    :params      [{:name                         community-name
                   :description                  community-name
                   :color                        "#887af9"
                   :historyArchiveSupportEnabled true
                   :membership                   constants/community-permissions-manual-accept
                   :pinMessageAllMembersEnabled  true}]}))

(defn create-token-gated-permission!
  [{:keys [token-symbol permission-type amount-in-wei]} community-id]
  (rpc/call
   {:method      "wakuext_createCommunityTokenPermission2"
    :js-response true
    :params      [{:communityId   community-id
                   :type          permission-type
                   :tokenCriteria [((token-criteria token-symbol) amount-in-wei)]}]}))

(defn create-community-from-description
  [{:keys [community-name
           community-permissions
           channel-list]}]
  (let [create-community-permissions-fn (map
                                         #(response->community-id-fn
                                           (partial create-token-gated-permission! %))
                                         community-permissions)
        create-channels-fn              (map
                                         #(response->community-id-fn
                                           (partial create-community-channel! %))
                                         channel-list)]
    (as-> (create-community! community-name) $
      (apply p/chain $ create-community-permissions-fn)
      (apply p/chain $ create-channels-fn)
      (p/then $ #(rf/dispatch [:hide-bottom-sheet]))
      (p/catch $ #(log/error "failed to create community e2e" {:error %})))))


(rf/defn create-token-gated-community
  {:events [:e2e/create-token-gated-community]}
  [_]
  (create-community-from-description (community-descriptions :token-gated))
  nil)

(rf/defn snt-admin-community
  {:events [:e2e/create-snt-admin-community]}
  [_]
  (create-community-from-description (community-descriptions :snt-admin))
  nil)

(rf/defn admin-and-member-community
  {:events [:e2e/create-admin-and-member-community]}
  [_]
  (create-community-from-description (community-descriptions :admin-and-member))
  nil)
