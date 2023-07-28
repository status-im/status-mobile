(ns status-im2.contexts.quo-preview.tabs.account-selector
  (:require [quo2.components.tabs.account-selector :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Show Label?:"
    :key   :show-label?
    :type  :boolean}
   {:label "Transparent Background?:"
    :key   :transparent?
    :type  :boolean}
   {:label "Account Text"
    :key   :account-text
    :type  :text}
   {:label "Label Text"
    :key   :label-text
    :type  :text}])

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

(defn cool-preview
  []
  (let [state (reagent/atom {:show-label?   true
                             :transparent?  false
                             :style         {:width :100%}
                             :account-text  "My Savings"
                             :account-emoji "🍑"
                             :label-text    "Label"})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:padding-vertical 60
          :align-items      :center}
         [quo2/account-selector @state]]]])))

(defn preview-this
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
