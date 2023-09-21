(ns status-im2.contexts.quo-preview.profile.select-profile
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.core :as quo]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label   "Customization Color"
    :key     :customization-color
    :type    :select
    :options (map (fn [color]
                    {:key   color
                     :value color})
                  (keys colors/customization))}
   {:label "Name"
    :key   :name
    :type  :text}
   {:label "Selected"
    :key   :selected?
    :type  :boolean}])

(defn preview-select-profile
  []
  (let [state     (reagent/atom {:selected?           false
                                 :name                "Alisher Yakupov"
                                 :customization-color :turquoise
                                 :profile-picture     (resources/get-mock-image :user-picture-male5)})
        selected? (reagent/cursor state [:selected?])]
    (fn []
      [preview/preview-container
       {:state      state
        :descriptor descriptor}
       [rn/view {:padding-bottom 150}
        [rn/view
         {:padding-vertical   60
          :flex-direction     :row
          :padding-horizontal 20
          :background-color   colors/neutral-90
          :justify-content    :center}
         [quo/select-profile (merge @state {:on-change #(reset! selected? %)})]]]])))
