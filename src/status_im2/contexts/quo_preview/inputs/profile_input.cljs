(ns status-im2.contexts.quo-preview.inputs.profile-input
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.blur :as blur]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key  :disabled?
    :type :boolean}
   {:key  :image-selected?
    :type :boolean}
   (preview/customization-color-option {:key :color})])

(defn view
  []
  (let [state          (reagent/atom {:color           :blue
                                      :image-selected? false
                                      :disabled?       false})
        max-length     24
        value          (reagent/atom "")
        on-change-text #(reset! value %)]
    (fn []
      [preview/preview-container {:state state :descriptor descriptor}
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
                                :on-change-text on-change-text}}]]])))
