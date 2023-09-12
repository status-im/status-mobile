(ns status-im2.contexts.quo-preview.navigation.top-nav
  (:require [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [quo2.core :as quo]
            [status-im2.contexts.quo-preview.preview :as preview]
            [status-im2.common.resources :as resources]
            [quo2.theme :as quo.theme]))

(def descriptor
  [{:key     :notification
    :type    :select
    :options [{:key :mention}
              {:key :notification}
              {:key :seen}]}
   {:key  :blur?
    :type :boolean}
   {:key  :jump-to?
    :type :boolean}
   {:key  :notification-count
    :type :number}
   (preview/customization-color-option)])

(defn preview
  []
  (let [state (reagent/atom {:noticication-count  0
                             :customization-color :blue})]
    (fn []
      (let [blur?               (:blur? @state)
            customization-color (:customization-color @state)
            jump-to?            (:jump-to? @state)
            notification        (:notification @state)
            notification-count  (:notification-count @state)]
        [preview/preview-container
         {:state      state
          :descriptor descriptor}
         [rn/view {:padding-bottom 150}
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
            {:container-style          {:flex 1 :z-index 2}
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

