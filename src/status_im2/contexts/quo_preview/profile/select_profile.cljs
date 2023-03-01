(ns status-im2.contexts.quo-preview.profile.select-profile
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.core :as quo]
            [status-im.react-native.resources :as resources]
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

(defn cool-preview
  []
  (let [state     (reagent/atom {:selected?           false
                                 :name                "Alisher Yakupov"
                                 :customization-color :turquoise
                                 :profile-picture     (resources/get-mock-image :user-picture-male5)})
        selected? (reagent/cursor state [:selected?])]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}
         [preview/customizer state descriptor]]
        [rn/view
         {:padding-vertical   60
          :flex-direction     :row
          :padding-horizontal 20
          :background-color   colors/neutral-90
          :justify-content    :center}
         [quo/select-profile (merge @state {:on-change #(reset! selected? %)})]]]])))

(defn preview-select-profile
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

