(ns status-im2.contexts.quo-preview.settings.section-label
  (:require
    [reagent.core :as reagent]
    [status-im2.contexts.quo-preview.preview :as preview]
    [quo2.core :as quo]))

(def ^:private descriptor
  [{:key  :section
    :type :text}
   {:key  :description
    :type :text}
   {:key  :blur?
    :type :boolean}])

(defn view
  []
  (let [state       (reagent/atom {:section     "Section label"
                                   :description ""
                                   :blur?       false})
        description (reagent/cursor state [:description])
        section     (reagent/cursor state [:section])
        blur?       (reagent/cursor state [:blur?])]
    (fn []
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :show-blur-background? true
        :blur-dark-only?       true
        :blur?                 @blur?
        :blur-height           150}
       [quo/section-label
        {:section     @section
         :description (when-not (empty? @description)
                        @description)
         :blur?       @blur?}]])))
