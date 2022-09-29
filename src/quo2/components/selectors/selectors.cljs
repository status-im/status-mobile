(ns quo2.components.selectors.selectors
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [quo.theme :as theme]
            [reagent.core :as reagent]
            [quo2.components.icon :as icons]))

(defn- get-color [checked? disabled?]
  (if checked?
    (colors/custom-color-by-theme
     :primary 50 60 (when disabled? 30) (when disabled? 30))
    (colors/theme-colors
     (colors/alpha colors/neutral-20 (if disabled? 0.4 1))
     (colors/alpha colors/neutral-70 (if disabled? 0.3 1)))))

(defn- handle-press [disabled? on-change checked?]
  (when (not disabled?)
    (fn []
      (swap! checked? not)
      (when on-change (on-change @checked?)))))

(defn checkbox-prefill [{:keys [default-checked?]}]
  (let [checked? (reagent/atom (or default-checked? false))]
    (fn [{:keys [on-change disabled? container-style]}]
      [rn/touchable-without-feedback
       {:on-press (handle-press disabled? on-change checked?)}
       [rn/view
        {:style (merge
                 container-style
                 {:height 21
                  :width 21
                  :border-radius 6
                  :background-color (colors/theme-colors
                                     (colors/alpha colors/neutral-20 (if disabled? 0.3 1))
                                     (colors/alpha colors/neutral-70 (if disabled? 0.3 1)))})
         :accessibility-label (str "checkbox-" (if @checked? "on" "off"))
         :accessibility-role  :checkbox}
        (when @checked?
          [rn/view {:style
                    {:height 20
                     :width 20}}
           [icons/icon :main-icons2/check
            {:size 20
             :color (colors/theme-colors
                     (colors/alpha colors/neutral-100 (if disabled? 0.3 1))
                     (colors/alpha colors/white (if disabled? 0.3 1)))}]])]])))

(defn checkbox [{:keys [default-checked?]}]
  (let [checked? (reagent/atom (or default-checked? false))]
    (fn [{:keys [on-change disabled? container-style]}]
      [rn/touchable-without-feedback
       {:on-press (handle-press disabled? on-change checked?)}
       [rn/view
        {:style  (merge
                  container-style
                  {:height 21
                   :width 21})}
        [rn/view
         {:style {:flex             1
                  :border-radius    6
                  :border-width     (if @checked? 0 1)
                  :background-color (if @checked?
                                      (get-color @checked? disabled?)
                                      (colors/theme-colors
                                       colors/white
                                       colors/neutral-80-opa-40))
                  :border-color     (if @checked? :none
                                        (get-color @checked? disabled?))}
          :accessibility-label (str "checkbox-" (if @checked? "on" "off"))
          :accessibility-role  :checkbox}
         (when @checked?
           [rn/view {:style
                     {:height 20
                      :width 20}}
            [icons/icon :main-icons2/check
             {:size 20
              :color (colors/alpha colors/white (if disabled? 0.3 1))}]])]]])))

(defn radio [{:keys [default-checked?]}]
  (let [checked? (reagent/atom (or default-checked? false))]
    (fn [{:keys [on-change disabled? container-style]}]
      [rn/touchable-without-feedback
       {:on-press (handle-press disabled? on-change checked?)}
       [rn/view
        {:style (merge
                 container-style
                 {:height 20
                  :width 20
                  :border-radius 20
                  :border-width 1
                  :border-color (get-color @checked? disabled?)
                  :background-color (colors/theme-colors colors/white
                                                         (colors/alpha colors/neutral-80 0.4))})
         :accessibility-label (str "radio-" (if @checked? "on" "off"))
         :accessibility-role  :checkbox}

        [rn/view {:style
                  {:margin-left :auto
                   :height 14
                   :width 14
                   :background-color (when @checked? (get-color @checked? disabled?))
                   :border-radius 20
                   :margin-right :auto
                   :margin-top :auto
                   :margin-bottom :auto}}]]])))

(defn toggle [{:keys [default-checked?]}]
  (let [checked? (reagent/atom (or default-checked? false))]
    (fn [{:keys [on-change disabled? container-style]}]
      [rn/touchable-without-feedback
       {:on-press (handle-press disabled? on-change checked?)}
       [rn/view
        {:style (merge
                 container-style
                 {:height 20
                  :width 30
                  :border-radius 20
                  :background-color (get-color @checked? disabled?)})
         :accessibility-label (str "toggle-" (if @checked? "on" "off"))
         :accessibility-role  :checkbox}
        [rn/view {:style
                  {:margin-left (if @checked? 12 2)
                   :height 16
                   :width 16
                   :background-color (colors/alpha colors/white
                                                   (if (theme/dark?)
                                                     (if disabled? 0.3 1)
                                                     1))
                   :border-radius 20
                   :margin-right :auto
                   :margin-top :auto
                   :margin-bottom :auto}}]]])))
