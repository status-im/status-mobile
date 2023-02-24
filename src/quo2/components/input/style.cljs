(ns quo2.components.input.style
  (:require [quo2.foundations.colors :as colors]))

;; TODO: remove duplicates
(def status-colors
  {:light      {:default  {:border-color        colors/neutral-20
                           :placeholder-color   colors/neutral-40
                           :cursor-color        (get-in colors/customization [:blue 50])
                           :text-color          colors/neutral-100
                           :icon-color          colors/neutral-50
                           :password-icon-color colors/neutral-100}
                :focus    {:border-color        colors/neutral-40
                           :placeholder-color   colors/neutral-30
                           :cursor-color        (get-in colors/customization [:blue 50])
                           :text-color          colors/neutral-100
                           :icon-color          colors/neutral-50
                           :password-icon-color colors/neutral-100
                           }
                :error    {:border-color        colors/danger-opa-40
                           :placeholder-color   colors/neutral-40
                           :cursor-color        (get-in colors/customization [:blue 50])
                           :text-color          colors/neutral-100
                           :icon-color          colors/neutral-50
                           :password-icon-color colors/neutral-100}

                :disabled {:border-color        colors/neutral-20
                           :placeholder-color   colors/neutral-40
                           :cursor-color        (get-in colors/customization [:blue 50]) ;; Not used
                           :text-color          colors/neutral-40
                           :icon-color          colors/neutral-50
                           :password-icon-color colors/neutral-50}
                }
   ;;
   :light-blur {:default  {:border-color        colors/neutral-80-opa-10
                           :placeholder-color   colors/neutral-80-opa-40
                           :cursor-color        (get-in colors/customization [:blue 50])
                           :text-color          colors/neutral-100
                           :icon-color          colors/neutral-80-opa-70
                           :password-icon-color colors/neutral-100}
                :focus    {:border-color        colors/neutral-80-opa-20
                           :placeholder-color   colors/neutral-80-opa-20
                           :cursor-color        (get-in colors/customization [:blue 50])
                           :text-color          colors/neutral-100
                           :icon-color          colors/neutral-80-opa-70
                           :password-icon-color colors/neutral-100}
                :error    {:border-color        colors/danger-opa-40
                           :placeholder-color   colors/neutral-80-opa-40
                           :cursor-color        (get-in colors/customization [:blue 50])
                           :text-color          colors/neutral-100
                           :icon-color          colors/neutral-80-opa-70
                           :password-icon-color colors/neutral-100}

                :disabled {:border-color        colors/neutral-80-opa-10
                           :placeholder-color   colors/neutral-80-opa-30
                           :cursor-color        (get-in colors/customization [:blue 50]) ;; Not used
                           :text-color          colors/neutral-80-opa-30
                           :icon-color          colors/neutral-80-opa-70
                           :password-icon-color colors/neutral-80-opa-70}
                }
   ;;
   :dark       {:default  {:border-color        colors/neutral-80
                           :placeholder-color   colors/neutral-50
                           :cursor-color        (get-in colors/customization [:blue 60])
                           :text-color          colors/white
                           :icon-color          colors/neutral-40
                           :password-icon-color colors/white
                           }
                :focus    {:border-color        colors/neutral-60
                           :placeholder-color   colors/neutral-60
                           :cursor-color        (get-in colors/customization [:blue 60])
                           :text-color          colors/white
                           :icon-color          colors/neutral-40
                           :password-icon-color colors/white}
                :error    {:border-color        colors/danger-opa-40
                           :placeholder-color   colors/white-opa-40
                           :cursor-color        (get-in colors/customization [:blue 60])
                           :text-color          colors/white
                           :icon-color          colors/neutral-40
                           :password-icon-color colors/white}

                :disabled {:border-color        colors/neutral-80
                           :placeholder-color   colors/neutral-40
                           :cursor-color        (get-in colors/customization [:blue 50]) ;; Not used
                           :text-color          colors/neutral-40
                           :icon-color          colors/neutral-40
                           :password-icon-color colors/neutral-40}}
   ;;
   :dark-blur  {:default  {:border-color        colors/white-opa-10
                           :placeholder-color   colors/white-opa-40
                           :cursor-color        colors/white
                           :text-color          colors/white
                           :icon-color          colors/white-opa-70
                           :password-icon-color colors/white}
                :focus    {:border-color        colors/white-opa-40
                           :placeholder-color   colors/white-opa-20
                           :cursor-color        colors/white
                           :text-color          colors/white
                           :icon-color          colors/white-opa-70
                           :password-icon-color colors/white}
                :error    {:border-color        colors/danger-opa-40
                           :placeholder-color   colors/white-opa-40
                           :cursor-color        colors/white
                           :text-color          colors/white
                           :icon-color          colors/white-opa-70
                           :password-icon-color colors/white}

                :disabled {:border-color        colors/white-opa-10
                           :placeholder-color   colors/white-opa-20
                           :cursor-color        (get-in colors/customization [:blue 50]) ;; Not used
                           :text-color          colors/white-opa-20
                           :icon-color          colors/white-opa-70
                           :password-icon-color colors/white-opa-70}
                }})

(defn input
  [colors-by-status left-icon?]
  {:height           40
   :border-width     1
   :border-color     (:border-color colors-by-status)
   :padding-vertical 9
   :padding-left     (if left-icon? 40 16)
   :padding-right    40
   :border-radius    14
   :color            (:text-color colors-by-status)})

(def right-icon-touchable-area
  {:position         :absolute
   :top              0
   :right            0
   :bottom           0
   :width            32
   :padding-vertical 9
   :padding-right    12
   :justify-content  :center
   :align-items      :center})

(defn password-icon [colors-by-status]
  {:size  20
   :color (:password-icon-color colors-by-status)})

(defn icon [colors-by-status]
  {:size  20
   :color (:icon-color colors-by-status)})
