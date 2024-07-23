(ns status-im.contexts.preview.quo.wallet.token-input
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as resources]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def networks
  [{:source (resources/get-network :arbitrum)}
   {:source (resources/get-network :optimism)}
   {:source (resources/get-network :ethereum)}])

(def title "Max: 200 SNT")

(def descriptor
  [{:key     :token
    :type    :select
    :options [{:key :eth}
              {:key :snt}]}
   {:key     :currency
    :type    :select
    :options [{:key :usd}
              {:key :eur}]}
   {:key  :error?
    :type :boolean}
   {:key  :allow-selection?
    :type :boolean}])

(defn view
  []
  (let [state     (reagent/atom {:token               :eth
                                 :currency            :usd
                                 :conversion          0.02
                                 :networks            networks
                                 :title               title
                                 :customization-color :blue
                                 :show-keyboard?      false
                                 :allow-selection?    true})
        value     (reagent/atom "")
        set-value (fn [v]
                    (swap! value str v))
        delete    (fn [_]
                    (swap! value #(subs % 0 (dec (count %)))))]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :full-screen?              true
        :component-container-style {:flex            1
                                    :justify-content :space-between}}
       [quo/token-input (assoc @state :value @value)]
       [quo/numbered-keyboard
        {:container-style {:padding-bottom (safe-area/get-top)}
         :left-action     :dot
         :delete-key?     true
         :on-press        set-value
         :on-delete       delete}]])))
