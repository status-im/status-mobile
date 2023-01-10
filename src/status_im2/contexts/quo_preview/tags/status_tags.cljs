(ns status-im2.contexts.quo-preview.tags.status-tags
  (:require [quo2.components.tags.status-tags :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [i18n.i18n :as i18n]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def status-tags-options
  {:label   "Status"
   :key     :status
   :type    :select
   :options [{:value "Positive"
              :key   :positive}
             {:value "Negative"
              :key   :negative}
             {:value "Pending"
              :key   :pending}]})

(def descriptor
  [status-tags-options
   {:label   "Size"
    :key     :size
    :type    :select
    :options [{:value "Small"
               :key   :small}
              {:value "Large"
               :key   :large}]}])

(defn cool-preview
  []
  (let [state (reagent/atom {:status :positive
                             :size   :small})]
    (fn []
      (let [props (cond-> @state
                    (= :positive (:status @state)) (assoc :status
                                                          {:label (i18n/label :positive)
                                                           :type  :positive})
                    (= :negative (:status @state)) (assoc :status
                                                          {:label (i18n/label :negative)
                                                           :type  :negative})
                    (= :pending (:status @state))  (assoc :status
                                                          {:label (i18n/label :pending)
                                                           :type  :pending}))]
        [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
         [rn/view {:padding-bottom 150}
          [rn/view {:flex 1}
           [preview/customizer state descriptor]]
          [rn/view
           {:padding-vertical 60
            :flex-direction   :row
            :justify-content  :center}
           [quo2/status-tag props]]]]))))

(defn preview-status-tags
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white
                                           colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                      1
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])
