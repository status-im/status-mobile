(ns status-im.contexts.preview.quo.wallet.wallet-overview
  (:require
    [quo.components.wallet.wallet-overview.schema :refer [?schema]]
    [quo.core :as quo]
    [quo.foundations.resources :as quo.resources]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.contexts.preview.quo.preview-generator :as preview-gen]))

(def descriptor (preview-gen/schema->descriptor ?schema))

(def networks-list
  [{:source (quo.resources/get-network :ethereum)}
   {:source (quo.resources/get-network :optimism)}
   {:source (quo.resources/get-network :arbitrum)}])

(defn view
  []
  (let [state (reagent/atom {:state             :default
                             :time-frame        :one-week
                             :metrics           :positive
                             :balance           "€0.00"
                             :date              "20 Nov 2023"
                             :begin-date        "16 May"
                             :end-date          "25 May"
                             :currency-change   "€0.00"
                             :percentage-change "0.00%"})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical 60
                                    :flex-direction   :row
                                    :justify-content  :center}}
       [quo/wallet-overview
        (assoc @state
               :networks          networks-list
               :dropdown-on-press #(js/alert "On pressed dropdown"))]])))
