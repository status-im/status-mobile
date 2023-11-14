(ns status-im2.contexts.quo-preview.wallet.progress-bar
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :state
    :type    :select
    :options [{:key :pending}
              {:key :confirmed}
              {:key :finalized}
              {:key :error}]}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:state               :pending
                             :customization-color :blue})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-top 40
                                    :align-items :center}}
       [quo/progress-bar @state]])))
