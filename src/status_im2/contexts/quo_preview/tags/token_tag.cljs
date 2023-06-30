(ns status-im2.contexts.quo-preview.tags.token-tag
  (:require [quo2.components.tags.token-tag :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]))

(def descriptor
  [{:label   "Size:"
    :key     :size
    :type    :select
    :options [{:key   :big
               :value "big"}
              {:key   :small
               :value "small"}]}
   {:label   "Value:"
    :key     :value
    :type    :select
    :options [{:key   0
               :value "0"}
              {:key   10
               :value "10"}
              {:key   100
               :value "100"}
              {:key   1000
               :value "1000"}
              {:key   10000
               :value "10000"}]}
   {:label "Is Sufficient:"
    :key   :sufficient?
    :type  :boolean}
   {:label "Is Purchasable:"
    :key   :purchasable?
    :type  :boolean}
   {:label   "Token:"
    :key     :token
    :type    :select
    :options [{:key   "ETH"
               :value "ETH"}
              {:key   "SNT"
               :value "SNT"}]}])

(def eth-token (resources/get-token :eth))
(def snt-token (resources/get-token :snt))

(defn cool-preview
  []
  (let [state (reagent/atom {:size :big :value 10 :token "ETH" :sufficient? false :purchasable? false})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [quo2/token-tag
          (merge @state
                 {:token-img-src (if (= (get-in @state [:token]) "ETH") eth-token snt-token)})]]]])))

(defn preview-token-tag
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
