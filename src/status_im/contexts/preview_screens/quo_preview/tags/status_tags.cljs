(ns status-im.contexts.preview-screens.quo-preview.tags.status-tags
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]
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

(defn view
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
        [preview/preview-container
         {:state                 state
          :show-blur-background? true
          :blur?                 (:blur? @state)
          :descriptor            descriptor}
         [rn/view {:style {:flex-direction :row}}
          [quo/status-tag props]]]))))
