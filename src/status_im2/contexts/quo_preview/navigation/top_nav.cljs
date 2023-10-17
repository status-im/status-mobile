(ns status-im2.contexts.quo-preview.navigation.top-nav
  (:require
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [reagent.core :as reagent]
    [status-im2.common.resources :as resources]
    [status-im2.contexts.quo-preview.preview :as preview]))

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

(defn view
  []
  (let [state (reagent/atom {:notification-count  0
                             :customization-color :blue})]
    (fn []
      (let [blur?               (:blur? @state)
            customization-color (:customization-color @state)
            jump-to?            (:jump-to? @state)
            notification        (:notification @state)
            notification-count  (:notification-count @state)]
        [preview/preview-container
         {:state                     state
          :descriptor                descriptor
          :blur?                     (and blur? (not jump-to?))
          :show-blur-background?     (and blur? (not jump-to?))
          :component-container-style {:padding-vertical   60
                                      :padding-horizontal 20
                                      :background-color   (when jump-to? colors/neutral-100)}}
         [quo/top-nav
          {:container-style          {:flex 1 :z-index 2}
           :max-unread-notifications 99
           :blur?                    blur?
           :notification             notification
           :customization-color      customization-color
           :notification-count       notification-count
           :jump-to?                 jump-to?
           :avatar-props             {:online?         true
                                      :full-name       "Test User"
                                      :profile-picture (resources/mock-images :user-picture-female2)}
           :avatar-on-press          #(js/alert "avatar pressed")
           :scan-on-press            #(js/alert "scan pressed")
           :activity-center-on-press #(js/alert "activity-center pressed")
           :qr-code-on-press         #(js/alert "qr pressed")}]]))))

