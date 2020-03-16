(ns status-im.ui.screens.keycard.components.keycard-animation
  (:require [status-im.ui.components.react :as react]
            [reagent.core :as reagent]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.animation :as animation]
            [status-im.hardwallet.card :as keycard-nfc]
            [status-im.react-native.resources :as resources]
            [status-im.ui.components.colors :as colors]
            [taoensso.timbre :as log]))

(defn circle [{:keys [animation-value color size]}]
  [react/animated-view
   {:style {:width            size
            :height           size
            :position         "absolute"
            :background-color color
            :border-radius    (/ size 2)
            :opacity          (animation/interpolate
                               animation-value
                               {:inputRange [0 1 2]
                                :outputRange [0.7 1 0]})
            :transform        [{:scale (animation/interpolate
                                        animation-value
                                        {:inputRange  [0 1]
                                         :outputRange [0.9 1]})}]}}])

(defn indicator-container [anim children]
  [react/animated-view
   {:style {:position         "absolute"
            :justify-content  :center
            :align-items      :center
            :border-radius    21
            :width            42
            :height           42
            :top              16.5
            :right            -24
            :shadow-offset    {:width 0 :height 2}
            :shadow-radius    16
            :elevation        8
            :shadow-opacity   0.1
            :shadow-color     "gba(0, 9, 26, 0.12)"
            :background-color colors/white
            :opacity          anim
            :transform        [{:scale (animation/interpolate
                                        anim
                                        {:inputRange  [0 1]
                                         :outputRange [0 1]})}]}}
   children])

(defn indicator [{:keys [state animation-value]}]
  [indicator-container animation-value
   (case @state
     :error
     [vector-icons/icon :main-icons/close {:color  colors/red
                                           :height 28
                                           :width  28}]
     :success
     [vector-icons/icon :main-icons/check {:color  colors/green
                                           :height 28
                                           :width  28}]
     :connected
     [vector-icons/icon :main-icons/check {:color  colors/blue
                                           :height 28
                                           :width  28}]
     :processing
     [react/activity-indicator {:color colors/blue}]

     nil)])

(defn animate-card-position [card-scale animation-value]
  {:transform [{:scale card-scale}
               {:translateX (animation/x animation-value)}
               {:translateY (animation/y animation-value)}]})

(defn card-colors [state]
  (case state
    (:init :awaiting)
    {:card-color "#2D2D2D"
     :key-color  "#27D8B9"
     :chip-color "#F0CC73"}
    (:connected :processing)
    {:card-color colors/blue
     :key-color  colors/white
     :chip-color colors/white}
    :success
    {:card-color colors/green
     :key-color  colors/white
     :chip-color colors/white}
    :error
    {:card-color colors/red
     :key-color  colors/white
     :chip-color colors/white}
    nil))

(defn card [{:keys [card-scale state indicator-value animation-value]}]
  (let [{:keys [card-color
                chip-color
                key-color]} (card-colors @state)]
    [react/animated-view
     {:style (merge
              (animate-card-position card-scale animation-value)
              {:height           80
               :width            120
               :border-radius    12
               :position         :absolute
               :shadow-offset    {:width 0 :height 2}
               :shadow-radius    16
               :elevation        8
               :shadow-opacity   0.1
               :shadow-color     "gba(0, 9, 26, 0.12)"
               :background-color card-color})}
     [react/animated-view
      {:style {:width            12
               :height           9
               :border-radius    3
               :left             19.5
               :top              30
               :background-color chip-color}}]
     [react/view
      {:style {:position        :absolute
               :justify-content :center
               :top             18
               :right           19.5
               :height          42
               :width           25}}
      [vector-icons/icon :main-icons/keycard-logo-big
       {:color  key-color
        :width  25
        :height 42}]]
     [indicator {:state           state
                 :animation-value indicator-value}]]))

(defn phone [{:keys [animation-value]}]
  [react/animated-view {:style {:position  :absolute
                                :bottom    0
                                :elevation 9
                                :opacity   (animation/interpolate
                                            animation-value
                                            {:inputRange  [0 1]
                                             :outputRange [0 0.9]})
                                :transform [{:translateY (animation/interpolate
                                                          animation-value
                                                          {:inputRange  [0 1]
                                                           :outputRange [125 10]})}]}}
   [react/image
    {:source (resources/get-image :onboarding-phone)
     :style  {:height 125
              :width  86}}]])

(def circle-easing    (animation/bezier 0.455 0.03 0.515 0.955))
(def card-easing      (animation/bezier 0.77 0 0.175 1))

(defn- circle-animation [animation-value to delay]
  (animation/timing animation-value
                    {:toValue  to
                     :delay    delay
                     :duration 1000
                     :easing   circle-easing}))

(defn start-animation
  [{:keys [small medium big card-scale phone
           indicator card state]}]
  (animation/set-value indicator 0)
  (animation/set-value phone 0)
  (let [phone-enter-at 7000
        resets         (animation/timing card-scale
                                         {:toValue  0.66
                                          :duration 1000
                                          :easing   card-easing})
        card-loop      (animation/anim-loop
                        (animation/anim-sequence
                         [(animation/timing card
                                            {:toValue  #js {:x -30
                                                            :y 30}
                                             :duration 1000
                                             :easing   card-easing})
                          (animation/timing card
                                            {:toValue  {:x 45
                                                        :y 65}
                                             :duration 1000
                                             :delay    2000
                                             :easing   card-easing})
                          (animation/timing card
                                            {:toValue  #js {:x -30
                                                            :y 105}
                                             :duration 1000
                                             :delay    2000
                                             :easing   card-easing})
                          (animation/anim-delay 2000)]))
        phone-entrance (animation/anim-sequence
                        [(animation/anim-delay phone-enter-at)
                         (animation/parallel
                          [(animation/timing card-scale
                                             {:toValue  0.528
                                              :duration 1000
                                              :easing   card-easing})
                           (animation/timing phone
                                             {:toValue  1
                                              :duration 1000
                                              :easing   card-easing})
                           card-loop])])
        circles        (animation/anim-loop
                        (animation/parallel
                         [(animation/anim-sequence
                           [(circle-animation small 1 0)
                            (circle-animation small 0 0)])
                          (animation/anim-sequence
                           [(circle-animation medium 1 200)
                            (circle-animation medium 0 0)])
                          (animation/anim-sequence
                           [(circle-animation big 1 400)
                            (circle-animation big 0 0)])]))
        animation      (animation/parallel
                        [resets
                         phone-entrance
                         circles])]
    (reset! state :init)
    ;; TODO(Ferossgp): Think how to improve that, this creates desync of state with animation
    (js/setTimeout #(compare-and-set! state :init :awaiting)
                   phone-enter-at)
    (animation/start animation)))

(defn on-error [{:keys [state restart]}]
  (reset! state :error)
  (js/setTimeout #(when (= @state :error)
                    (restart)) 3000))

(defn on-success [{:keys [state]}]
  (reset! state :success))

(defn on-connect
  [{:keys [state card small indicator
           medium big card-scale phone]}]
  (let [connect-animation (animation/parallel
                           [(animation/timing card-scale
                                              {:toValue 1
                                               :timing  1000
                                               :easing  card-easing})
                            (animation/timing indicator
                                              {:toValue 1
                                               :timing  1000
                                               :easing  card-easing})
                            (animation/timing small
                                              {:toValue 2
                                               :timing  1000
                                               :easing  circle-easing})
                            (animation/timing medium
                                              {:toValue 2
                                               :timing  1000
                                               :easing  circle-easing})
                            (animation/timing big
                                              {:toValue 2
                                               :timing  1000
                                               :easing  circle-easing})
                            (animation/timing phone
                                              {:toValue 0
                                               :timing  1000
                                               :easing  card-easing})
                            (animation/timing card
                                              {:toValue #js {:x 0
                                                             :y 0}
                                               :timing  3000
                                               :easing  card-easing})])]
    (reset! state :connected)
    (js/setTimeout #(compare-and-set! state :connected :processing)
                   2000)
    (animation/start connect-animation)))

(defn animated-circles [{:keys [state connected? on-card-connected on-card-disconnected]}]
  (let [animation-small     (animation/create-value 0)
        animation-medium    (animation/create-value 0)
        animation-big       (animation/create-value 0)
        animation-card      (animation/create-value-xy #js {:x 0
                                                            :y 0})
        card-scale          (animation/create-value 0.66)
        animation-phone     (animation/create-value 0)
        animation-indicator (animation/create-value 0)
        on-start-animation  #(start-animation
                              {:state      state
                               :small      animation-small
                               :medium     animation-medium
                               :big        animation-big
                               :phone      animation-phone
                               :card       animation-card
                               :card-scale card-scale
                               :indicator  animation-indicator})
        on-card-connected   #(do
                               (on-card-connected)
                               (on-connect
                                {:state      state
                                 :indicator  animation-indicator
                                 :card       animation-card
                                 :card-scale card-scale
                                 :phone      animation-phone
                                 :small      animation-small
                                 :medium     animation-medium
                                 :big        animation-big}))
        on-success          #(on-success
                              {:state state})
        on-error            #(do
                               (on-card-disconnected)
                               (on-error
                                {:state   state
                                 :restart on-start-animation}))
        listeners           (atom [])]
    (reagent/create-class
     {:component-did-mount
      (fn []
        (doseq [listener @listeners]
          (keycard-nfc/remove-event-listener listener))

        (reset! listeners [(keycard-nfc/on-card-connected on-card-connected)
                           (keycard-nfc/on-card-disconnected on-error)])

        (on-start-animation)

        (when connected?
          (on-card-connected)))
      :component-will-unmount
      (fn []
        (doseq [listener @listeners]
          (keycard-nfc/remove-event-listener listener)))
      :render
      (fn []
        [react/view {:style {:position        :absolute
                             :top             0
                             :bottom          0
                             :left            0
                             :right           0
                             :justify-content :center
                             :align-items     :center}}

         [circle {:animation-value animation-big
                  :size            200
                  :color           "#F1F4FF"}]
         [circle {:animation-value animation-medium
                  :size            140
                  :color           "#E3E8FA"}]
         [circle {:animation-value animation-small
                  :size            80
                  :color           "#D2D9F0"}]

         [card {:animation-value animation-card
                :state           state
                :indicator-value animation-indicator
                :card-scale      card-scale}]

         [phone {:animation-value animation-phone}]])})))
