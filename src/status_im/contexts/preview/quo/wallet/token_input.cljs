(ns status-im.contexts.preview.quo.wallet.token-input
  (:require
    [quo.components.wallet.token-input.schema :refer [?schema]]
    [quo.core :as quo]
    [quo.foundations.resources :as resources]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.contexts.preview.quo.preview-generator :as preview-gen]))

(def descriptor (preview-gen/schema->descriptor ?schema {:exclude-keys #{:value}}))

(def networks
  [{:source (resources/get-network :arbitrum)}
   {:source (resources/get-network :optimism)}
   {:source (resources/get-network :ethereum)}])

(defn view
  []
  (let [state     (reagent/atom {:token               :eth
                                 :currency            :usd
                                 :conversion          0.02
                                 :networks            networks
                                 :title               "Max: 200 SNT"
                                 :customization-color :blue
                                 :show-keyboard?      false})
        value     (reagent/atom "")
        set-value (fn [v]
                    (swap! value str v))
        delete    (fn [_]
                    (swap! value #(subs % 0 (dec (count %)))))]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:flex               1
                                    :padding-horizontal 0
                                    :justify-content    :space-between}}
       [quo/token-input (assoc @state :value @value)]
       [quo/numbered-keyboard
        {:container-style {:padding-bottom (safe-area/get-top)}
         :left-action     :dot
         :delete-key?     true
         :on-press        set-value
         :on-delete       delete}]])))
