(ns status-im.contexts.preview-screens.quo-preview.community.token-gating
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

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
     :amount      200
     :sufficient? true
     :loading?    loading?}
    {:symbol       "MANA"
     :amount       10
     :sufficient?  sufficient?
     :purchasable? true
     :loading?     loading?}
    {:symbol      "RARE"
     :amount      10
     :sufficient? sufficient?
     :loading?    loading?}]
   (when many-tokens?
     [{:symbol      "FXC"
       :amount      20
       :sufficient? true
       :loading?    loading?}
      {:symbol      "SNT"
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
          :amount      20
          :sufficient? true}
         {:symbol      "USDT"
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
