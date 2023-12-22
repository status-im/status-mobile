(ns status-im.contexts.preview-screens.quo-preview.tabs.tabs
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :size
    :type    :select
    :options [{:key   32
               :value "32"}
              {:key   24
               :value "24"}]}
   {:key :unread-indicators? :type :boolean}
   {:key :scrollable? :type :boolean}])

(defn generate-tab-items
  [length unread-indicators?]
  (for [index (range length)]
    ^{:key index}
    {:id                index
     :label             (str "Tab " (inc index))
     :notification-dot? (and unread-indicators? (zero? (rem index 2)))}))

(defn view
  []
  (let [state (reagent/atom {:size        32
                             :scrollable? false})]
    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:padding-vertical   60
                                    :padding-horizontal 20
                                    :flex-direction     :row
                                    :justify-content    :center}}
       [quo/tabs
        (merge @state
               {:default-active 1
                :data           (generate-tab-items (if (:scrollable? @state) 15 4)
                                                    (:unread-indicators? @state))
                :on-change      #(println "Active tab" %)}
               (when (:scrollable? @state)
                 {:scroll-on-press?    true
                  :fade-end-percentage 0.4
                  :fade-end?           true}))]])))
