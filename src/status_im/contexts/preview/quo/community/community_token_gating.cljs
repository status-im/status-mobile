(ns status-im.contexts.preview.quo.community.community-token-gating
  (:require [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im.common.resources :as resources]
            [status-im.contexts.preview.quo.preview :as preview]
            [utils.i18n :as i18n]))

(def descriptor
  [{:key     :role
    :type    :select
    :options [{:key :member}
              {:key :admin}
              {:key :token-master}
              {:key :token-owner}]}
   {:key  :satisfied?
    :type :boolean}
   (preview/customization-color-option {:key :community-color})])

(def tokens
  {:member
   [[{:symbol "ETH" :sufficient? true :collectible? false :amount "0.8" :img-src nil}]
    [{:symbol "ETH" :sufficient? false :collectible? false :amount "1" :img-src nil}
     {:symbol "STT" :sufficient? false :collectible? false :amount "10" :img-src nil}]]
   :admin
   [[{:symbol "ETH" :sufficient? true :collectible? false :amount "2" :img-src nil}]]
   :token-master
   [[{:symbol       "TMANI"
      :sufficient?  true
      :collectible? true
      :amount       nil
      :img-src      (resources/mock-images :collectible1)}]]
   :token-owner
   [[{:symbol       "TOANI"
      :sufficient?  true
      :collectible? true
      :amount       nil
      :img-src      (resources/mock-images :collectible)}]]})

(def role
  {:member       (i18n/label :t/member)
   :admin        (i18n/label :t/admin)
   :token-master (i18n/label :t/token-master)
   :token-owner  (i18n/label :t/token-owner)})

(defn view
  []
  (let [state (reagent/atom {:role            :member
                             :satisfied?      true
                             :community-color :blue})]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :show-blur-background? false}
       [quo/community-token-gating
        {:community-color (:community-color @state)
         :role            ((:role @state) role)
         :satisfied?      (:satisfied? @state)
         :on-press        #(js/alert "On press 'Request to join'")
         :on-press-info   #(js/alert "On press info")
         :tokens          ((:role @state) tokens)}]])))
