(ns status-im2.contexts.quo-preview.community.token-gating
  (:require [quo2.core :as quo]
            [quo2.foundations.resources :as resources]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Tokens sufficient?"
    :key   :sufficient?
    :type  :boolean}
   {:key  :many-tokens?
    :type :boolean}
   {:key  :loading?
    :type :boolean}
   {:key  :condition?
    :type :boolean}
   {:key  :padding?
    :type :boolean}])

(defn join-gate-options-base
  [sufficient? many-tokens? loading?]
  (into
   [{:symbol      "KNC"
     :img-src     (resources/get-token :knc)
     :amount      200
     :sufficient? true
     :loading?    loading?}
    {:symbol       "MANA"
     :img-src      (resources/get-token :mana)
     :amount       10
     :sufficient?  sufficient?
     :purchasable? true
     :loading?     loading?}
    {:symbol      "RARE"
     :img-src     (resources/get-token :rare)
     :amount      10
     :sufficient? sufficient?
     :loading?    loading?}]
   (when many-tokens?
     [{:symbol      "FXC"
       :img-src     (resources/get-token :fxc)
       :amount      20
       :sufficient? true
       :loading?    loading?}
      {:symbol      "SNT"
       :img-src     (resources/get-token :snt)
       :amount      10000
       :sufficient? sufficient?
       :loading?    loading?}])))

(defn get-mocked-props
  [props]
  (let [{:keys [sufficient? condition? many-tokens? padding? loading?]} props]
    {:tokens
     (if condition?
       [(join-gate-options-base sufficient?
                                many-tokens?
                                loading?)
        [{:symbol      "FXC"
          :img-src     (resources/get-token :fxc)
          :amount      20
          :sufficient? true}
         {:symbol      "USDT"
          :img-src     (resources/get-token :usdt)
          :amount      20
          :sufficient? false}]]
       [(join-gate-options-base sufficient?
                                many-tokens?
                                loading?)])
     :padding? padding?}))

(defn view
  []
  (let [state (reagent/atom {:sufficient?  false
                             :many-tokens? false
                             :condition?   false
                             :padding?     false})]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
       [quo/token-requirement-list (get-mocked-props @state)]])))
