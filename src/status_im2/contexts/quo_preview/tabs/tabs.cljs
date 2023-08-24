(ns status-im2.contexts.quo-preview.tabs.tabs
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
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

(defn cool-preview
  []
  (let [state (reagent/atom {:size        32
                             :scrollable? false})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
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

(defn preview-tabs
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
