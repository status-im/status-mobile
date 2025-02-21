(ns status-im.contexts.communities.actions.accounts-selection.effects
  (:require
    [clojure.string :as string]
    [promesa.core :as promesa]
    [schema.core :as schema]
    [status-im.common.json-rpc.events :as rpc]
    [status-im.contexts.wallet.rpc :as wallet-rpc]
    [utils.re-frame :as rf]
    [utils.signatures :as signatures]))

(def ^:private ?addresses-to-reveal
  [:or [:set string?]
   [:sequential string?]])

(defn- generate-requests-for-signing
  [pub-key community-id addresses-to-reveal]
  (rpc/call-async "wakuext_generateJoiningCommunityRequestsForSigning"
                  false
                  pub-key
                  community-id
                  (or addresses-to-reveal [])))

(schema/=> generate-requests-for-signing
  [:=>
   [:catn
    [:community-id string?]
    [:pub-key string?]
    [:addresses-to-reveal ?addresses-to-reveal]]
   :any])

(rf/reg-fx
 :effects.community/generate-requests-for-signing
 (fn [{:keys [pub-key community-id addresses-to-reveal on-success on-error]}]
   (-> (generate-requests-for-signing pub-key community-id addresses-to-reveal)
       (promesa/then (fn [requests]
                       (promesa/all
                        (for [{:keys [data account]} requests]
                          (promesa/let [hashed-data (wallet-rpc/hash-message-eip-191 data)]
                            {:message hashed-data
                             :address (string/lower-case account)})))))
       (promesa/then on-success)
       (promesa/catch on-error))))

(defn- edit-shared-addresses-for-community
  [community-id signatures addresses-to-reveal airdrop-address _share-future-addresses?]
  (rpc/call-async "wakuext_editSharedAddressesForCommunity"
                  true
                  {:communityId       community-id
                   :signatures        (map signatures/adjust-legacy-ecdsa-signature signatures)
                   :addressesToReveal addresses-to-reveal
                   :airdropAddress    airdrop-address}))

(schema/=> edit-shared-addresses-for-community
  [:=>
   [:catn
    [:community-id string?]
    [:signatures [:sequential string?]]
    [:addresses-to-reveal ?addresses-to-reveal]
    [:airdrop-address string?]
    [:_share-future-addresses? [:maybe boolean?]]]
   :any])

(rf/reg-fx :effects.community/edit-shared-addresses
 (fn [{:keys [on-success on-error community-id signatures addresses-to-reveal airdrop-address
              share-future-addresses?]}]
   (-> (edit-shared-addresses-for-community community-id
                                            signatures
                                            addresses-to-reveal
                                            airdrop-address
                                            share-future-addresses?)
       (promesa/then on-success)
       (promesa/catch on-error))))

(defn- request-to-join
  [community-id signatures addresses-to-reveal airdrop-address share-future-addresses?]
  (rpc/call-async "wakuext_requestToJoinCommunity"
                  true
                  {:communityId          community-id
                   :signatures           (map signatures/adjust-legacy-ecdsa-signature signatures)
                   :addressesToReveal    addresses-to-reveal
                   :airdropAddress       airdrop-address
                   :shareFutureAddresses share-future-addresses?}))

(schema/=> request-to-join
  [:=>
   [:catn
    [:community-id string?]
    [:signatures [:sequential string?]]
    [:addresses-to-reveal ?addresses-to-reveal]
    [:airdrop-address string?]
    [:share-future-addresses? [:maybe boolean?]]]
   :any])

(rf/reg-fx :effects.community/request-to-join
 (fn [{:keys [on-success on-error community-id signatures addresses-to-reveal airdrop-address
              share-future-addresses?]}]
   (-> (request-to-join community-id
                        signatures
                        addresses-to-reveal
                        airdrop-address
                        share-future-addresses?)
       (promesa/then on-success)
       (promesa/catch on-error))))
