(ns status-im.contexts.preview.quo.wallet.account-overview
  (:require
    [quo.components.wallet.account-overview.schema :refer [?schema]]
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]
    [status-im.contexts.preview.quo.preview-generator :as preview-gen]))

(def descriptor (preview-gen/schema->descriptor ?schema))

(defn view
  []
  (let [state (reagent/atom {:metrics              :positive
                             :currency-change      "€0.00"
                             :percentage-change    "0.00%"
                             :current-value        "€0.00"
                             :account-name         "Diamond Hand"
                             :time-frame           :custom
                             :time-frame-string    "16 May"
                             :time-frame-to-string "25 May"
                             :account              :default
                             :customization-color  :blue
                             :container-style      {:padding-top        24
                                                    :padding-horizontal 20
                                                    :padding-bottom     20}})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical 60
                                    :flex-direction   :row
                                    :justify-content  :center}}
       [quo/account-overview @state]])))
