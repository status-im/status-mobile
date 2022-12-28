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

(defn cool-preview
  []
  (let [state (reagent/atom {:override-theme (theme/get-theme)
                             :blur?          false})]
    (fn []
      [rn/view
       {:style {:margin-bottom      50
                :padding-vertical   16
                :padding-horizontal 20}}
       [preview/customizer state descriptor]
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

(defn preview
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-90)
            :flex             1}}
   [rn/flat-list
    {:style                     {:flex 1}
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])
