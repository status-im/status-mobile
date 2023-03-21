(ns status-im2.contexts.quo-preview.community.token-gating
  (:require [quo.previews.preview :as preview]
            [quo.react-native :as rn]
            [quo2.components.community.token-gating :as quo2]
            [quo2.foundations.colors :as colors]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]))

(def styles
  {:container-sandbox {:flex                    1
                       :padding-vertical        20
                       :border-top-left-radius  20
                       :border-top-right-radius 20}})

(def descriptor
  [{:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key   :community
               :value "Community"}
              {:key   :channel
               :value "Channel"}]}
   {:label "Tokens sufficient:"
    :key   :is-sufficient?
    :type  :boolean}
   {:label "Many tokens:"
    :key   :many-tokens?
    :type  :boolean}
   {:label "Membership request denied:"
    :key   :membership-request-denied?
    :type  :boolean}])

(defn join-gate-options-base
  [is-sufficient? many-tokens?]
  (into
   [{:token          "KNC"
     :token-img-src  (resources/get-token :knc)
     :amount         200
     :is-sufficient? true}
    {:token           "MANA"
     :token-img-src   (resources/get-token :mana)
     :amount          10
     :is-sufficient?  is-sufficient?
     :is-purchasable? true}
    {:token          "RARE"
     :token-img-src  (resources/get-token :rare)
     :amount         10
     :is-sufficient? is-sufficient?}]
   (when many-tokens?
     [{:token          "FXC"
       :token-img-src  (resources/get-token :fxc)
       :amount         20
       :is-sufficient? true}
      {:token          "SNT"
       :token-img-src  (resources/get-token :snt)
       :amount         10000
       :is-sufficient? is-sufficient?}])))

(defn write-gate-options-base
  [is-sufficient?]
  [{:token          "KNC"
    :token-img-src  (resources/get-token :knc)
    :amount         200
    :is-sufficient? true}
   {:token           "DAI"
    :token-img-src   (resources/get-token :dai)
    :amount          20
    :is-purchasable? true
    :is-sufficient?  is-sufficient?}
   {:token          "ETH"
    :token-img-src  (resources/get-token :eth)
    :amount         0.5
    :is-sufficient? is-sufficient?}])

(defn get-mocked-props
  [props]
  (let [{:keys [type is-sufficient? many-tokens? membership-request-denied?]} props]
    (if (= type :community)
      {:community {:name                     "Ethereum"
                   :community-color          "#14044d"
                   :community-avatar-img-src (resources/get-token :eth)
                   :gates                    {:join (if
                                                      many-tokens?
                                                      [(join-gate-options-base is-sufficient?
                                                                               many-tokens?)
                                                       [{:token          "FXC"
                                                         :token-img-src  (resources/get-token :fxc)
                                                         :amount         20
                                                         :is-sufficient? true}
                                                        {:token          "USDT"
                                                         :token-img-src  (resources/get-token :usdt)
                                                         :amount         20
                                                         :is-sufficient? false}]]
                                                      (join-gate-options-base is-sufficient?
                                                                              many-tokens?))}}}
      {:channel {:name "onboarding"
                 :community-color (colors/custom-color :pink 50)
                 :community-text-color colors/white
                 :emoji "🍑"
                 :emoji-background-color "#F38888"
                 :on-enter-channel #(js/alert
                                     "Entered channel - Wuhuu!! You successfully entered the channel :)")
                 :membership-request-denied? membership-request-denied?
                 :gates {:read  (into [{:token          "KNC"
                                        :token-img-src  (resources/get-token :knc)
                                        :amount         200
                                        :is-sufficient? true}
                                       {:token           "MANA"
                                        :token-img-src   (resources/get-token :mana)
                                        :amount          10
                                        :is-sufficient?  is-sufficient?
                                        :is-purchasable? true}
                                       {:token          "RARE"
                                        :token-img-src  (resources/get-token :rare)
                                        :amount         10
                                        :is-sufficient? is-sufficient?}]
                                      (when many-tokens?
                                        [{:token          "FXC"
                                          :token-img-src  (resources/get-token :fxc)
                                          :amount         20
                                          :is-sufficient? true}
                                         {:token          "SNT"
                                          :token-img-src  (resources/get-token :snt)
                                          :amount         10000
                                          :is-sufficient? is-sufficient?}]))
                         :write (if
                                  many-tokens?
                                  [(write-gate-options-base is-sufficient?)
                                   [{:token          "FXC"
                                     :token-img-src  (resources/get-token :fxc)
                                     :amount         20
                                     :is-sufficient? true}
                                    {:token          "MANA"
                                     :token-img-src  (resources/get-token :mana)
                                     :amount         10
                                     :is-sufficient? is-sufficient?}
                                    {:token          "USDT"
                                     :token-img-src  (resources/get-token :usdt)
                                     :amount         20
                                     :is-sufficient? false}]]
                                  (write-gate-options-base is-sufficient?))}}})))

(def state
  (reagent/atom {:type                       :channel
                 :is-sufficient?             false
                 :many-tokens?               false
                 :membership-request-denied? false}))

(defn preview-token-gating
  []
  (let [preview-props (get-mocked-props @state)]
    [rn/view
     {:style {:background-color (colors/theme-colors
                                 colors/neutral-10
                                 colors/neutral-80)
              :flex             1}}
     [rn/view {:style {:flex 1}}
      [rn/view
       {:style {:position :absolute
                :left     0
                :right    0
                :top      0}}
       [preview/customizer state descriptor]]]
     [rn/view {:height (if (= (:type @state) :community) 280 495) :margin-top 20}
      [rn/view
       {:style (merge
                (get styles :container-sandbox)
                {:background-color (colors/theme-colors
                                    colors/white
                                    colors/neutral-90)})}
       [quo2/token-gating preview-props]]]]))
