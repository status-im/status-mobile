(ns status-im.contexts.profile.settings.header.avatar
  (:require [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [status-im.contexts.profile.settings.header.style :as style]
            ["react-native-reanimated" :as animated]
            ))

#_(def header-extrapolation-option
  {:extrapolateLeft  "clamp"
   :extrapolateRight "clamp"})


(defn f-avatar [a]
  (let [sv         (reanimated/use-shared-value 0)
        THIS-THING (animated/useAnimatedStyle
                    (fn HEEEEEEY []
                      (js* "'worklet;'")
                      (js/console.log "hola")
                      #js{:transform #js[#js{:translateX (.-value sv)}]}
                      ))
        ;image-scale-animation       (reanimated/interpolate scroll-y
        ;                                                    scroll-animation-input-range
        ;                                                    [1 0.4]
        ;                                                    header-extrapolation-option)
        ;image-top-margin-animation  (reanimated/interpolate scroll-y
        ;                                                    scroll-animation-input-range
        ;                                                    [0 20]
        ;                                                    header-extrapolation-option)
        ;image-side-margin-animation (reanimated/interpolate scroll-y
        ;                                                    scroll-animation-input-range
        ;                                                    [0 -20]
        ;                                                    header-extrapolation-option)
        ;theme                       (quo.theme/get-theme)
        ]
    [rn/view
     [quo/button {:size     56
                  :above    :i/placeholder
                  :on-press (fn []
                              (set! (.-value sv) (reanimated/with-timing 200 #js{:duration 2000}))
                              )}
      "Trigger change"]

     [reanimated/view {:style [THIS-THING ;{:transform [{:translate-x sv}]}
                               {:background-color "red"
                                :width            20
                                :height           20}]
                       #_(style/avatar-container theme
                                                 image-scale-animation
                                                 image-top-margin-animation
                                                 image-side-margin-animation)}
      #_[quo/user-avatar
         {:full-name           full-name
          :online?             online?
          :profile-picture     profile-picture
          :status-indicator?   true
          :ring?               true
          :customization-color customization-color
          :size                :big}]]

     ]))

(defn view
  [props]
  [:f> f-avatar props])
