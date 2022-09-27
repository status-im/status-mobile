(ns quo2.components.selectors.selectors
  (:require [reagent.core :as reagent]
            [quo.react :as react]
            [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [cljs-bean.core :as bean]
            [quo2.components.icon :as icons]))

(defn get-color [value disabled?]
  (if value
    (colors/theme-colors
     (colors/custom-color :primary 50 (when disabled? 30))
     (colors/custom-color :primary 60 (when disabled? 30)))
    (colors/theme-colors
     (colors/alpha colors/neutral-20 (if disabled? 0.4 1))
     (colors/alpha colors/neutral-70 (if disabled? 0.3 1)))))

(defn checkbox-prefill-view [props]
  (let [{:keys [value onChange disabled? containerStyle]} (bean/bean props)]
    (reagent/as-element
     [rn/touchable-without-feedback
      {:on-press (when (and onChange (not disabled?)) onChange)}
      [rn/view
       {:style        (merge
                       (js->clj containerStyle)
                       {:height 21
                        :width 21
                        :border-radius 6
                        :background-color (colors/theme-colors
                                           (colors/alpha colors/neutral-20 (if disabled? 0.3 1))
                                           (colors/alpha colors/neutral-70 (if disabled? 0.3 1)))})
        :accessibility-label (str "checkbox-" (if value "on" "off"))
        :accessibility-role  :checkbox}
       (when value
         [rn/view {:style {:height 20
                           :width 20}}
          [icons/icon :main-icons2/check
           {:size 20
            :color (colors/theme-colors
                    (colors/alpha colors/neutral-100 (if disabled? 0.3 1))
                    (colors/alpha colors/white (if disabled? 0.3 1)))}]])]])))

(defn checkbox-view [props]
  (let [{:keys [value onChange disabled? containerStyle]} (bean/bean props)]
    (reagent/as-element
     [rn/touchable-without-feedback
      {:on-press (when (and onChange (not disabled?)) onChange)}
      [rn/view
       {:style        (merge
                       (js->clj containerStyle)
                       {:height (if value 20 21)
                        :width (if value 20 21)
                        :border-radius 6
                        :border-width (if value 0 1)
                        :background-color (if value
                                            (get-color value disabled?)
                                            (colors/theme-colors
                                             colors/white
                                             (colors/alpha colors/neutral-80 0.4)))
                        :border-color (if value :none
                                          (get-color value disabled?))})

        :accessibility-label (str "checkbox-" (if value "on" "off"))
        :accessibility-role  :checkbox}
       (when value
         [rn/view {:style {:height 20
                           :width 20}}
          [icons/icon :main-icons2/check
           {:size 20
            :color (colors/alpha colors/white (if disabled? 0.3 1))}]])]])))

(defn radio-view [props]
  (let [{:keys [value onChange disabled? containerStyle]} (bean/bean props)]
    (reagent/as-element
     [rn/touchable-without-feedback
      {:on-press (when (and onChange (not disabled?)) onChange)}
      [rn/view
       {:style        (merge
                       (js->clj containerStyle)
                       {:height 20
                        :width 20
                        :border-radius 40
                        :border-width 1
                        :border-color (get-color value disabled?)
                        :background-color (colors/theme-colors colors/white
                                                               (colors/alpha colors/neutral-80 0.4))})
        :accessibility-label (str "radio-" (if value "on" "off"))
        :accessibility-role  :checkbox}

       [rn/view {:style {:margin-left :auto
                         :height 14
                         :width 14
                         :background-color (when value (get-color value disabled?))
                         :border-radius 40
                         :margin-right :auto
                         :margin-top :auto
                         :margin-bottom :auto}}]]])))

(defn toggle-view [props]
  (let [{:keys [value onChange disabled? containerStyle]} (bean/bean props)]
    (reagent/as-element
     [rn/touchable-without-feedback
      {:on-press (when (and onChange (not disabled?)) onChange)}
      [rn/view
       {:style        (merge
                       (js->clj containerStyle)
                       {:height 20
                        :width 30
                        :border-radius 40
                        :background-color (get-color value disabled?)})
        :accessibility-label (str "toggle-" (if value "on" "off"))
        :accessibility-role  :checkbox}
       [rn/view {:style {:margin-left (if value 12 2)
                         :height 16
                         :width 16
                         :background-color (colors/alpha colors/white 1)
                         :border-radius 40
                         :margin-right :auto
                         :margin-top :auto
                         :margin-bottom :auto}}]]])))

(def radio (reagent/adapt-react-class (react/memo radio-view)))
(def toggle (reagent/adapt-react-class (react/memo toggle-view)))
(def checkbox (reagent/adapt-react-class (react/memo checkbox-view)))
(def checkbox-prefill (reagent/adapt-react-class (react/memo checkbox-prefill-view)))
