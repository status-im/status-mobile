(ns status-im2.contexts.quo-preview.inputs.profile-input
  (:require [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]
            [quo.react-native :as rn]))

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
      [preview/preview-container
       {:state                 state
        :descriptor            descriptor
        :blur?                 true
        :show-blur-background? true}
       [rn/view
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
