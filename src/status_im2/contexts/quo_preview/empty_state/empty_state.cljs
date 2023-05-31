(ns status-im2.contexts.quo-preview.empty-state.empty-state
  (:require [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]))

(def descriptor
  [{:label "Title:"
    :key   :title
    :type  :text}
   {:label "Description:"
    :key   :description
    :type  :text}
   {:label   "Image:"
    :key     :image
    :type    :select
    :options [{:key   :no-contacts-light
               :value "No contacts light"}
              {:key   :no-contacts-dark
               :value "No contacts dark"}
              {:key   :no-messages-light
               :value "No messages light"}
              {:key   :no-messages-dark
               :value "No messages dark"}]}
   {:label "Upper button text"
    :key   :upper-button-text
    :type  :text}
   {:label "Lower button text"
    :key   :lower-button-text
    :type  :text}
   {:label "Blur:"
    :key   :blur?
    :type  :boolean}])

(defn cool-preview
  []
  (let [state (reagent/atom {:image             :no-messages-light
                             :title             "A big friendly title"
                             :description       "Some cool description will be here"
                             :blur?             false
                             :upper-button-text "Send community link"
                             :lower-button-text "Invite friends to Status"})]
    (fn []
      [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
       [rn/view {:padding-bottom 150}
        [preview/customizer state descriptor]
        [rn/view
         {:style {:margin-vertical  24
                  :background-color (when (:blur? @state) colors/neutral-95)}}
         [preview/blur-view
          {:style                 {:width       "100%"
                                   :align-items :center
                                   :top         (if (:blur? @state) 32 16)
                                   :position    (if (:blur? @state)
                                                  :absolute
                                                  :relative)}
           :height                300
           :show-blur-background? (:blur? @state)
           :blur-view-props       (when (:blur? @state)
                                    {:overlay-color colors/neutral-80-opa-80})}

          [rn/view {:style {:flex 1 :width "100%"}}
           [quo/empty-state
            (-> @state
                (assoc :upper-button
                       {:text     (:upper-button-text @state)
                        :on-press #(js/alert "Upper button")})
                (assoc :lower-button
                       {:text     (:lower-button-text @state)
                        :on-press #(js/alert "Lower button")})
                (update :image resources/get-image))]]]]]])))

(defn preview-empty-state
  []
  [rn/view
   {:style {:flex             1
            :background-color (colors/theme-colors colors/white colors/neutral-95)}}
   [rn/flat-list
    {:style                        {:flex 1}
     :nestedScrollEnabled          true
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       :id}]])
