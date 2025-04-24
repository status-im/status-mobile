(ns status-im.contexts.preview.quo.wallet.swap-input
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as resources]
    [react-native.core :as rn]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:type    :select
    :key     :type
    :options [{:key :pay}
              {:key :receive}]}
   {:type    :select
    :key     :status
    :options [{:key :default}
              {:key :disabled}
              {:key :loading}]}
   {:type    :select
    :key     :value
    :options [{:key :token}
              {:key :fiat}]}
   {:type :boolean
    :key  :error?}
   {:type :boolean
    :key  :enable-swap?}
   {:type :boolean
    :key  :show-approval-label?}
   (preview/customization-color-option)])

(defn view
  []
  (let [[state set-state] (rn/use-state {:type                 :pay
                                         :error?               false
                                         :token                "SNT"
                                         :customization-color  :blue
                                         :show-approval-label? false
                                         :enable-swap?         true
                                         :status               :default
                                         :currency-symbol      "€"})
        [value set-value] (rn/use-state "")
        on-press          (fn [v] (set-value (str value v)))
        delete            (fn [] (set-value #(subs % 0 (dec (count %)))))]
    [preview/preview-container
     {:state      state
      :set-state  set-state
      :descriptor descriptor}
     [quo/swap-input
      (assoc state
             :on-swap-press #(js/alert "Swap Pressed")
             :on-token-press #(js/alert "Token Pressed")
             :on-max-press #(js/alert "Max Pressed")
             :value value
             :fiat-value (str (.toFixed (* value 0.3) 2))
             :container-style {:margin-bottom 20}
             :network-tag-props {:title    "Max: 200 SNT"
                                 :networks [{:source (resources/get-network :ethereum)}]}
             :approval-label-props
             {:status              :approve
              :token-value         "10"
              :button-props        {:on-press
                                    #(js/alert "Approve Pressed")}
              :customization-color (:customization-color state)
              :token-symbol        "SNT"})]
     [quo/numbered-keyboard
      {:left-action :dot
       :delete-key? true
       :on-press    on-press
       :on-delete   delete}]]))
