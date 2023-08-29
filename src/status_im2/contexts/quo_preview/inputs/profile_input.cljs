(ns status-im2.contexts.quo-preview.inputs.profile-input
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.core :as quo]
            [react-native.blur :as blur]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "disabled?"
    :key   :disabled?
    :type  :boolean}
   {:label "image selected?"
    :key   :image-selected?
    :type  :boolean}
   {:label   "Custom Color"
    :key     :color
    :type    :select
    :options (map (fn [color]
                    (let [k (get color :name)]
                      {:key k :value k}))
                  (quo/picker-colors))}])

(defn cool-preview
  []
  (let [state          (reagent/atom {:color           :blue
                                      :image-selected? false
                                      :disabled?       false})
        max-length     24

        value          (reagent/atom "")
        on-change-text #(reset! value %)]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [rn/view {:flex 1}]
        [preview/customizer state descriptor]
        [blur/view
         {:background-color  colors/neutral-80-opa-80
          :flex-direction    :row
          :margin-horizontal 20
          :justify-content   :center}
         [quo/profile-input
          {:default-value       ""
           :on-change-text      on-change-text
           :customization-color (:color @state)
           :placeholder         "Your Name"
           :on-press            #(js/alert "show image selector")
           :image-picker-props  {:profile-picture (when (:image-selected? @state)
                                                    (resources/get-mock-image :user-picture-male5))
                                 :full-name       @value}
           :title-input-props   {:disabled?      (:disabled? @state)
                                 :max-length     max-length
                                 :on-change-text on-change-text}}]]]])))

(defn preview-profile-input
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white
                                           colors/neutral-90)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
