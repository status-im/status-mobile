(ns status-im.contexts.preview.quo.community.community-detail-token-gating
  (:require [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im.common.resources :as resources]
            [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key     :role
    :type    :select
    :options [{:key :one-role}
              {:key :two-roles}
              {:key :three-roles}
              {:key :four-roles}]}])

(def roles
  {:member
   {:role 2
    :role-text "Member"
    :satisfied? true
    :tokens
    [[{:symbol "ETH" :sufficient? true :collectible? false :loading? false :amount 0.8 :img-src nil}]
     [{:symbol "ETH" :sufficient? false :collectible? false :loading? false :amount 1 :img-src nil}
      {:symbol "STT" :sufficient? false :collectible? false :loading? false :amount 10 :img-src nil}]]}
   :admin
   {:role 1
    :role-text "Admin"
    :satisfied? true
    :tokens
    [[{:symbol "ETH" :sufficient? true :collectible? false :loading? false :amount 2 :img-src nil}]]}
   :token-master
   {:role 5
    :role-text "Token Master"
    :satisfied? true
    :tokens
    [[{:symbol       "TMANI"
       :sufficient?  true
       :collectible? true
       :loading?     false
       :amount       nil
       :img-src      (resources/mock-images :collectible1)}]]}
   :token-owner
   {:role 6
    :role-text "Token Owner"
    :satisfied? true
    :tokens
    [[{:symbol       "TOANI"
       :sufficient?  true
       :collectible? true
       :loading?     false
       :amount       nil
       :img-src      (resources/mock-images :collectible)}]]}})

(def permissions
  {:one-role    [(:member roles)]
   :two-roles   [(:admin roles) (:member roles)]
   :three-roles [(:token-master roles) (:admin roles) (:member roles)]
   :four-roles  [(:token-owner roles) (:token-master roles) (:admin roles) (:member roles)]})

(defn view
  []
  (let [state (reagent/atom {:role :one-role})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 (:blur? @state)
        :show-blur-background? true}
       [quo/community-detail-token-gating {:permissions ((:role @state) permissions)}]])))
