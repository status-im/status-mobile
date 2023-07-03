(ns status-im2.contexts.quo-preview.loaders.skeleton
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Content:"
    :key     :content
    :type    :select
    :options [{:key   :list-items
               :value "List items"}
              {:key   :notifications
               :value "Notifications"}
              {:key   :messages
               :value "Messages"}]}
   {:label "Blur?"
    :key   :blur?
    :type  :boolean}
   {:label "Animated?" ;; Not implemented yet
    :key   :animated?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:content       :messages
                             :blur?         false
                             :animated?     false
                             :parent-height 600})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         [quo/static-skeleton @state]]]])))

(defn preview-skeleton
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
