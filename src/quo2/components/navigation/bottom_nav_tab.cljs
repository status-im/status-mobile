(ns quo2.components.navigation.bottom-nav-tab
  (:require [quo.react-native :as rn]
            [reagent.core :as reagent]
            [quo2.foundations.colors :as colors]
            [quo2.components.icon :as quo2.icons]
            [quo2.components.counter.counter :as counter]))

(defn toggle-background-color [background-color press-out? pass-through?]
  (let [color (cond
                press-out?    nil
                pass-through? colors/white-opa-5
                :else         colors/neutral-70)]
    (reset! background-color color)))

(defn bottom-nav-tab
  "[bottom-nav-tab opts]
   opts
   {:icon              :main-icons2/communities
    :selected?         true/false
    :notification?     true/false
    :notification-type :unread/:counter
    :counter-label     number
    :on-press          bottom-tab on-press function
    :pass-through?     true/false
  "
  [_]
  (let [background-color (reagent/atom nil)]
    (fn [{:keys [icon selected? notification? notification-type counter-label on-press pass-through?]}]
      [rn/touchable-without-feedback
       {:on-press       on-press
        :on-press-in    #(toggle-background-color background-color false pass-through?)
        :on-press-out   #(toggle-background-color background-color true pass-through?)}
       [rn/view {:style {:width            90
                         :height           40
                         :background-color @background-color
                         :border-radius    10}}
        [rn/hole-view {:style {:padding-left 33
                               :padding-top  8}
                       :holes  (if (and notification? (= notification-type :unread))
                                 [{:x 49 :y 4 :width 12 :height 12 :borderRadius 6}]
                                 [])}
         [quo2.icons/icon
          icon
          {:size 24
           :color (cond
                    selected?     colors/white
                    pass-through? colors/white-opa-40
                    :else         colors/neutral-50)}]]
        (when notification?
          (if (= notification-type :counter)
            [counter/counter {:outline             false
                              :override-text-color colors/white
                              :override-bg-color   colors/primary-50
                              :style               {:position :absolute
                                                    :left     48
                                                    :top      2}}
             counter-label]
            [rn/view {:style {:width            8
                              :height           8
                              :border-radius    4
                              :top              6
                              :left             51
                              :position         :absolute
                              :background-color colors/primary-50}}]))]])))
