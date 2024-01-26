(ns status-im.contexts.communities.actions.addresses-for-permissions.events
  (:require
    [schema.core :as schema]
    [taoensso.timbre :as log]
    [utils.re-frame :as rf]))

(defn get-permissioned-balances
  [{[event-id _] :original-event} [community-id]]
  {:fx [[:json-rpc/call
         [{:method     :wakuext_getCommunityPermissionedBalances
           :params     [{:communityId community-id}]
           :on-success [:communities/get-permissioned-balances-success community-id]
           :on-error   (fn [error]
                         (log/error "failed to get balances"
                                    {:community-id community-id
                                     :event        event-id
                                     :error        error}))}]]]})

(schema/=> get-permissioned-balances
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args [:schema [:catn [:community-id :string]]]]]
   [:map {:closed true}
    [:fx
     [:tuple
      [:tuple [:= :json-rpc/call] :schema.common/rpc-call]]]]])

(rf/reg-event-fx :communities/get-permissioned-balances get-permissioned-balances)

(defn get-permissioned-balances-success
  [{:keys [db]} [community-id response]]
  {:db (assoc-in db [:communities/permissioned-balances community-id] response)})

(def ^:private ?account-address keyword?)

(def ^:private ?permissioned-balances-response
  [:map-of
   ?account-address
   [:sequential
    [:map {:closed true}
     [:type int?]
     [:symbol string?]
     [:decimals int?]
     [:amount string?]]]])

(schema/=> get-permissioned-balances-success
  [:=>
   [:catn
    [:cofx :schema.re-frame/cofx]
    [:args
     [:schema
      [:?
       [:catn
        [:community-id string?]
        [:response ?permissioned-balances-response]]]]]]
   [:map {:closed true}
    [:db map?]]])

(rf/reg-event-fx :communities/get-permissioned-balances-success get-permissioned-balances-success)
