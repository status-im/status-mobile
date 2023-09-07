(ns status-im2.contexts.quo-preview.tabs.tabs
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Size:"
    :key     :size
    :type    :select
    :options [{:key   32
               :value "32"}
              {:key   24
               :value "24"}]}
   {:label "Show unread indicators?"
    :key   :unread-indicators?
    :type  :boolean}
   {:label "Scrollable:"
    :key   :scrollable?
    :type  :boolean}])

(defn generate-tab-items
  [length unread-indicators?]
  (for [index (range length)]
    ^{:key index}
    {:id                index
     :label             (str "Tab " (inc index))
     :notification-dot? (and unread-indicators? (zero? (rem index 2)))}))

(defn preview-tabs
  []
  (let [state (reagent/atom {:size        32
                             :scrollable? false})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:padding-bottom 150}
        [rn/view
         {:padding-vertical   60
          :padding-horizontal 20
          :flex-direction     :row
          :justify-content    :center}
         [quo/tabs
          (merge @state
                 {:default-active 1
                  :data           (generate-tab-items (if (:scrollable? @state) 15 4)
                                                      (:unread-indicators? @state))
                  :on-change      #(println "Active tab" %)}
                 (when (:scrollable? @state)
                   {:scroll-on-press?    true
                    :fade-end-percentage 0.4
                    :fade-end?           true}))]]]])))
