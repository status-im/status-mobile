(ns status-im2.contexts.quo-preview.wallet.token-input
  (:require
    [quo2.core :as quo]
    [quo2.foundations.resources :as resources]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def networks
  [{:source (resources/get-network :arbitrum)}
   {:source (resources/get-network :optimism)}
   {:source (resources/get-network :ethereum)}])

<<<<<<< HEAD
<<<<<<< HEAD
(def title "Max: 200 SNT")

=======
>>>>>>> afdc05d94 (token input followup)
=======
(def title "Max: 200 SNT")

>>>>>>> ffc214f89 (lint)
(def descriptor
  [{:label   "Token:"
    :key     :token
    :type    :select
    :options [{:key   :eth
               :value "ETH"}
              {:key   :snt
               :value "SNT"}]}
   {:label   "Currency:"
    :key     :currency
    :type    :select
    :options [{:key   :usd
               :value "USD"}
              {:key   :eur
               :value "EUR"}]}])

(defn preview
  []
<<<<<<< HEAD
  (let [state (reagent/atom {:token               :eth
                             :currency            :usd
                             :conversion          0.02
                             :networks            networks
                             :title               title
                             :customization-color :blue})]
=======
  (let [state (reagent/atom {:token      :eth
                             :currency   :usd
                             :conversion 0.02
                             :networks   networks
<<<<<<< HEAD
                             :title      "Max: 200 SNT"})]
>>>>>>> afdc05d94 (token input followup)
=======
                             :title      title})]
>>>>>>> ffc214f89 (lint)
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view
        {:style {:flex               1
                 :padding-horizontal 20}}
        [rn/view
         {:style {:flex        1
                  :margin-top  50
                  :align-items :center}}
         [quo/token-input @state]]]])))
