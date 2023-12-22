(ns status-im.contexts.preview-screens.quo-preview.tabs.account-selector
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key :show-label? :type :boolean}
   {:key :transparent? :type :boolean}
   {:key :account-text :type :text}
   {:key :label-text :type :text}])

;; keeping this unused data structure in the code for now
;; will reference them when I introduce multiple account support
;; and allow passing lists of accounts instead of just 1 account
(def single-account
  [{:account-text  "My Savings"
    :account-emoji "🍑"
    :label-text    "Label"}])

(def two-accounts
  [{:account-text  "My Savings"
    :account-emoji "🍑"
    :label-text    "Label"}
   {:account-text  "My Current"
    :account-emoji "🍎"
    :label-text    "Label 2"}])

(def many-accounts
  [{:account-text  "My Savings"
    :account-emoji "🍑"
    :label-text    "Label"}
   {:account-text  "My Current"
    :account-emoji "🍎"
    :label-text    "Label 2"}
   {:account-text  "My Reimbursment"
    :account-emoji "🍟"
    :label-text    "Label 3"}])

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
