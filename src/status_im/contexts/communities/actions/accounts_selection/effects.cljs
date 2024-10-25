(ns status-im.contexts.communities.actions.accounts-selection.effects
  (:require
    [promesa.core :as promesa]
    [schema.core :as schema]
    [status-im.common.json-rpc.events :as rpc]
    [utils.re-frame :as rf]
    [utils.security.core :as security]))

(defn- generate-requests-for-signing
  [pub-key community-id addresses-to-reveal]
  (promesa/create
   (fn [p-resolve p-reject]
     (rpc/call
      {:method     :wakuext_generateJoiningCommunityRequestsForSigning
       :params     [pub-key community-id addresses-to-reveal]
       :on-success p-resolve
       :on-error   #(p-reject (str "failed to generate requests for signing\n" %))}))))

(defn- sign-data
  [sign-params password]
  (promesa/create
   (fn [p-resolve p-reject]
     (rpc/call
      {:method     :wakuext_signData
       :params     [(map #(assoc % :password (security/safe-unmask-data password)) sign-params)]
       :on-success p-resolve
       :on-error   #(p-reject (str "failed to sign data\n" %))}))))

(defn- edit-shared-addresses-for-community
  [community-id signatures addresses-to-reveal airdrop-address _share-future-addresses?]
  (promesa/create
   (fn [p-resolve p-reject]
     (rpc/call
      {:method      :wakuext_editSharedAddressesForCommunity
       :params      [{:communityId       community-id
                      :signatures        signatures
                      :addressesToReveal addresses-to-reveal
                      :airdropAddress    airdrop-address}]
       :js-response true
       :on-success  p-resolve
       :on-error    p-reject}))))

(defn- request-to-join
  [community-id signatures addresses-to-reveal airdrop-address share-future-addresses?]
  (promesa/create
   (fn [p-resolve p-reject]
     (rpc/call
      {:method      :wakuext_requestToJoinCommunity
       :params      [{:communityId          community-id
                      :signatures           signatures
                      :addressesToReveal    addresses-to-reveal
                      :airdropAddress       airdrop-address
                      :shareFutureAddresses share-future-addresses?}]
       :js-response true
       :on-success  p-resolve
       :on-error    p-reject}))))

(defn- run-callback-or-event
  [callback-or-event result]
  (cond (fn? callback-or-event)
        (callback-or-event result)

        (vector? callback-or-event)
        (rf/dispatch (conj callback-or-event result))))

(defn- sign-and-call-endpoint
  [{:keys [community-id password pub-key
           addresses-to-reveal airdrop-address share-future-addresses?
           on-success on-error
           callback]}]
  (-> (promesa/let [sign-params (generate-requests-for-signing pub-key
                                                               community-id
                                                               addresses-to-reveal)
                    signatures  (sign-data sign-params password)
                    result      (callback community-id
                                          signatures
                                          addresses-to-reveal
                                          airdrop-address
                                          share-future-addresses?)]
        (run-callback-or-event on-success result))
      (promesa/catch #(run-callback-or-event on-error %))))

(schema/=> sign-and-call-endpoint
  [:=>
   [:cat
    [:map {:closed true}
     [:community-id string?]
     [:password [:maybe [:or string? security/?masked-password]]]
     [:pub-key string?]
     [:addresses-to-reveal
      [:or [:set string?]
       [:sequential string?]]]
     [:airdrop-address string?]
     [:share-future-addresses? boolean?]
     [:on-success [:or fn? :schema.re-frame/event]]
     [:on-error [:or fn? :schema.re-frame/event]]
     [:callback fn?]]]
   :any])

(rf/reg-fx :effects.community/edit-shared-addresses
 (fn [opts]
   (sign-and-call-endpoint
    (assoc opts :callback edit-shared-addresses-for-community))))

(rf/reg-fx :effects.community/request-to-join
 (fn [opts]
   (sign-and-call-endpoint
    (assoc opts :callback request-to-join))))
