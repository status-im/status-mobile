(ns status-im2.contexts.quo-preview.notifications.activity-logs-photos
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Photos Count:"
    :key     :count
    :type    :select
    :options [{:key   1
               :value "1"}
              {:key   2
               :value "2"}
              {:key   3
               :value "3"}
              {:key   4
               :value "4"}
              {:key   5
               :value "5"}
              {:key   6
               :value "6"}]}])

(def mock-photos
  [(resources/get-mock-image :photo1)
   (resources/get-mock-image :photo2)
   (resources/get-mock-image :photo3)
   (resources/get-mock-image :photo1)
   (resources/get-mock-image :photo2)
   (resources/get-mock-image :photo3)])

(defn preview-activity-logs-photos
  []
  (let [state (reagent/atom {:count 1})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:padding-bottom 150}
        [rn/view
         [quo/activity-logs-photos {:photos (take (:count @state) mock-photos)}]]]])))
