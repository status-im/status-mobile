(ns status-im2.contexts.quo-preview.settings.section-label
  (:require
    [react-native.core :as rn]
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

(defn preview
  []
  (let [state       (reagent/atom {:section     "Section label"
                                   :description ""
                                   :blur?       false})
        description (reagent/cursor state [:description])
        section     (reagent/cursor state [:section])
        blur?       (reagent/cursor state [:blur?])]

    (fn []
      [preview/preview-container
       {:state                     state
        :descriptor                descriptor
        :component-container-style {:align-items :center}}
       [rn/view
        {:style {:flex          1
                 :align-self    :stretch
                 :border-radius 24}}
        [preview/blur-view
         {:style                 {:flex               1
                                  :padding-horizontal 32
                                  :padding-vertical   32}
          :height                150
          :show-blur-background? @blur?}
         [quo/section-label
          {:section     @section
           :description (if (empty? @description)
                          nil
                          @description)
           :blur?       @blur?}]]]])))
