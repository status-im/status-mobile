(ns status-im2.contexts.quo-preview.tags.status-tags
  (:require [quo2.components.tags.status-tags :as quo2]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [utils.i18n :as i18n]))

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
               :key   :large}]}
   {:label "Blur?"
    :key   :blur?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:status :positive
                             :size   :small
                             :blur?  false})]
    (fn []
      (let [props (case (:status @state)
                    :positive (-> @state
                                  (assoc :status {:type :positive})
                                  (assoc :label (i18n/label :t/positive)))
                    :negative (-> @state
                                  (assoc :status {:type :negative})
                                  (assoc :label (i18n/label :t/negative)))
                    :pending  (-> @state
                                  (assoc :status {:type :pending})
                                  (assoc :label (i18n/label :t/pending))))]
        [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
         [rn/view {:padding-bottom 150}
          [rn/view {:flex 1}
           [preview/customizer state descriptor]]
          [preview/blur-view
           {:show-blur-background? (:blur? @state)
            :blur-view-props       {:blur-type     :dark
                                    :overlay-color colors/neutral-80-opa-80}
            :style                 {:align-self :center}} [quo2/status-tag props]]]]))))

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
