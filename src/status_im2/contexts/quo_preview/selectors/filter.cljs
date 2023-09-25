(ns status-im2.contexts.quo-preview.selectors.filter
  (:require [quo2.components.selectors.filter.view :as quo]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Theme"
    :key     :override-theme
    :type    :select
    :options [{:key   :dark
               :value "Dark"}
              {:key   :light
               :value "Light"}]}
   {:label "Blur?"
    :key   :blur?
    :type  :boolean}])

(defn preview
  []
  (let [state (reagent/atom {:override-theme (theme/get-theme)
                             :blur?          false})]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view
        {:style {:padding-vertical 60
                 :align-items      :center}}
        [rn/view
         {:style {:width            "100%"
                  :height           250
                  :align-items      :center
                  :justify-content  :center
                  :border-radius    20
                  :background-color (cond (= :dark (:override-theme @state))
                                          colors/neutral-95

                                          (= :light (:override-theme @state))
                                          colors/white)}}
         [quo/view @state]]]])))
