(ns status-im.contexts.preview.quo.tabs.account-selector
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview.quo.preview :as preview]))

(def descriptor
  [{:key :show-label? :type :boolean}
   {:key :transparent? :type :boolean}
   {:key :account-text :type :text}
   {:key :label-text :type :text}])

(defn view
  []
  (let [state (reagent/atom {:show-label?   true
                             :transparent?  false
                             :style         {:width :100%}
                             :account-text  "My Savings"
                             :account-emoji "🍑"
                             :label-text    "Label"})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical 60}}
       [quo/account-selector @state]])))
