(ns status-im.contexts.preview-screens.quo-preview.wallet.progress-bar
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :state
    :type    :select
    :options [{:key :pending}
              {:key :confirmed}
              {:key :finalized}
              {:key :error}]}
   {:key  :full-width?
    :type :boolean}
   {:key  :progressed-value
    :type :text}
   (preview/customization-color-option)])

(defn view
  []
  (let [state (reagent/atom {:state               :pending
                             :full-width?         false
                             :progressed-value    "10"
                             :customization-color :blue})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-top 40
                                    :align-items :center}}
       [rn/view {:style {:flex-direction :row}}
        [quo/progress-bar @state]]])))
