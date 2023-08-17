(ns status-im2.contexts.quo-preview.navigation.top-nav
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.core :as quo]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]
            [quo2.theme :as quo.theme]))

(def descriptor
  [{:label   "Notification"
    :key     :notification
    :type    :select
    :options [{:key   :mention
               :value "Mention"}
              {:key   :notification
               :value "Notification"}
              {:key   :seen
               :value "Seen"}
              {:key   :false
               :value "False"}]}
   {:label "Blur?"
    :key   :blur?
    :type  :boolean}
   {:label "Jump To?"
    :key   :jump-to?
    :type  :boolean}
   {:label "Notification Count"
    :key   :notification-count
    :type  :number}
   {:label   "Customization color:"
    :key     :customization-color
    :type    :select
    :options (map (fn [color]
                    (let [k (get color :name)]
                      {:key k :value k}))
                  (quo/picker-colors))}])

(defn cool-preview
  []
  (let [state (reagent/atom {:noticication-count  0
                             :customization-color :blue})]
    (fn []
      (let [blur?               (:blur? @state)
            customization-color (:customization-color @state)
            jump-to?            (:jump-to? @state)
            notification        (:notification @state)
            notification-count  (:notification-count @state)]
        [rn/touchable-without-feedback {:on-press rn/dismiss-keyboard!}
         [rn/view {:padding-bottom 150}
          [preview/customizer state descriptor]
          [rn/view
           {:padding-vertical   60
            :padding-horizontal 20
            :flex-direction     :row
            :align-items        :center}
           (when blur?
             [rn/image
              {:source (resources/get-mock-image (quo.theme/theme-value :light-blur-background
                                                                        :dark-blur-background))
               :style  {:position :absolute
                        :top      0
                        :left     0
                        :right    0
                        :bottom   0}}])
           (when jump-to?
             [rn/image
              {:background-color colors/neutral-100
               :style            {:position :absolute
                                  :top      0
                                  :left     0
                                  :right    0
                                  :bottom   0}}])
           [quo/top-nav
            {:container-style          {:flex 1}
             :max-unread-notifications 99
             :blur?                    blur?
             :notification             notification
             :customization-color      customization-color
             :notification-count       notification-count
             :jump-to?                 jump-to?
             :avatar-props             {:online?   true
                                        :full-name "Test User"}
             :avatar-on-press          #(js/alert "avatar pressed")
             :scan-on-press            #(js/alert "scan pressed")
             :activity-center-on-press #(js/alert "activity-center pressed")
             :qr-code-on-press         #(js/alert "qr pressed")}]]]]))))

(defn preview-top-nav
  []
  [rn/view
   {:background-color (colors/theme-colors colors/white colors/neutral-95)
    :flex             1}
   [rn/flat-list
    {:flex                         1
     :keyboard-should-persist-taps :always
     :header                       [cool-preview]
     :key-fn                       str}]])
