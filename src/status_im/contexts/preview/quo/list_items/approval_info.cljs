(ns status-im.contexts.preview.quo.list-items.approval-info
  (:require
    [quo.core :as quo]
    [quo.foundations.resources :as resources]
    [react-native.core :as rn]
    [status-im.common.resources :as common.resources]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:type    :select
    :key     :type
    :options [{:key :spending-cap}
              {:key :token-contract}
              {:key :account}
              {:key :spending-contract}
              {:key :network}
              {:key :date-signed}
              {:key :collectible}
              {:key :collectible-contract}
              {:key :address}
              {:key :community}]}
   {:type :text
    :key  :label}
   {:type :text
    :key  :description}
   {:type :boolean
    :key  :blur?}
   {:type :boolean
    :key  :unlimited-icon?}
   {:type :text
    :key  :button-label}
   {:type :text
    :key  :tag-label}
   {:type    :select
    :key     :option-icon
    :options [{:key   nil
               :value "None"}
              {:key :i/options}
              {:key :i/chevron-right}]}])

(defn- get-avatar-props
  [type]
  (case type
    :collectible          {:image (common.resources/get-mock-image :collectible2)}
    :account              {:customization-color :orange
                           :emoji               "😇"}
    :collectible-contract {:image (common.resources/get-mock-image :bored-ape)}
    :spending-contract    {:network-image (resources/get-network :ethereum)
                           :image         (resources/get-dapp :coingecko)}
    :date-signed          {:icon :i/signature}
    :address              {:customization-color :blue
                           :full-name           "0 x"}
    :community            {:image (common.resources/get-mock-image :status-logo)
                           :size  :size-32}
    {:image (common.resources/get-mock-image :status-logo)}))

(defn view
  []
  (let [[state set-state] (rn/use-state {:type            :spending-cap
                                         :label           "Label"
                                         :description     "Description"
                                         :blur?           false
                                         :unlimited-icon? false
                                         :button-label    "Edit"
                                         :tag-label       "31,283.77 EUR"
                                         :option-icon     :i/options})]
    [preview/preview-container
     {:state                 state
      :set-state             set-state
      :blur?                 (:blur? state)
      :show-blur-background? true
      :blur-dark-only?       true
      :descriptor            descriptor}
     [quo/approval-info
      (assoc state
             :button-icon     :i/edit
             :on-button-press #(js/alert "Button Pressed")
             :on-avatar-press #(js/alert "Token Pressed")
             :avatar-props    (get-avatar-props (:type state))
             :on-option-press #(js/alert "Option Pressed"))]]))
