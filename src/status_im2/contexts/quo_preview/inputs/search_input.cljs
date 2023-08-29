(ns status-im2.contexts.quo-preview.inputs.search-input
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:label "Value"
    :key   :value
    :type  :text}
   {:label "Blur"
    :key   :blur?
    :type  :boolean}
   {:label "Disabled"
    :key   :disabled?
    :type  :boolean}
   {:label   "Number of Tags"
    :key     :number-tags
    :type    :select
    :options (map (fn [n]
                    {:key n :value (str n)})
                  (range 0 5))}])

(defn example-tags
  [blur?]
  [[quo/context-tag
    {:blur?           blur?
     :size            24
     :profile-picture (resources/get-mock-image :user-picture-male5)
     :full-name       "alisher.eth"}]
   [quo/context-tag
    {:blur?           blur?
     :size            24
     :profile-picture (resources/get-mock-image :user-picture-male4)
     :full-name       "Pedro"}]
   [quo/context-tag
    {:blur?           blur?
     :size            24
     :profile-picture (resources/get-mock-image :user-picture-female2)
     :full-name       "Freya Odinson"}]])

(defn cool-preview
  []
  (let [state          (reagent/atom {:blur?       false
                                      :disabled?   false
                                      :number-tags 0
                                      :placeholder "Search..."
                                      :value       ""
                                      :on-clear    #(js/alert "Clear pressed")})
        on-change-text (fn [new-text]
                         (swap! state assoc :value new-text))]
    (fn []
      (let [tags (take (:number-tags @state) (example-tags (:blur? @state)))]
        [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
         [rn/view {:style {:padding-bottom 150}}
          [rn/view {:style {:flex 1}}
           [preview/customizer state descriptor]]
          [preview/blur-view
           {:style                 {:align-items     :center
                                    :margin-vertical 20
                                    :width           "100%"}
            :show-blur-background? (:blur? @state)}
           [rn/view {:style {:width "100%"}}
            [quo/search-input
             (assoc @state
                    :tags           tags
                    :on-change-text on-change-text)]]]]]))))

(defn preview-search-input
  []
  [rn/view
   {:style {:background-color (colors/theme-colors colors/white colors/neutral-95)
            :flex             1}}
   [rn/flat-list
    {:style                     {:flex 1}
     :keyboardShouldPersistTaps :always
     :header                    [cool-preview]
     :key-fn                    str}]])
