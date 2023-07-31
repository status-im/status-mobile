(ns status-im2.contexts.quo-preview.community.token-gating
  (:require
    [quo2.foundations.resources :as resources]
    [reagent.core :as reagent]
    [react-native.core :as rn]
    [status-im2.contexts.quo-preview.preview :as preview]
    [quo2.core :as quo]))

(def descriptor
  [{:label "Tokens sufficient"
    :key   :sufficient?
    :type  :boolean}
   {:label "Many tokens ?"
    :key   :many-tokens?
    :type  :boolean}
   {:label "Loading ?"
    :key   :loading?
    :type  :boolean}
   {:label "Сondition ?"
    :key   :condition?
    :type  :boolean}
   {:label "Padding ?"
    :key   :padding?
    :type  :boolean}])

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

(def state
  (reagent/atom {:sufficient?  false
                 :many-tokens? false
                 :condition?   false
                 :padding?     false}))

(defn preview-token-gating
  []
  (let [preview-props (get-mocked-props @state)]
    [rn/view {:flex 1}
     [rn/scroll-view {:style {:flex 1}}
      [preview/customizer state descriptor]]
     [rn/view {:padding-horizontal 20 :padding-vertical 20}
      [quo/token-requirement-list preview-props]]]))
