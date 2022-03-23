(ns quo2.components.counter
  (:require [quo.theme :as theme]
            [quo.react-native :as rn]
            [quo2.components.text :as text]
            [quo2.foundations.colors :as colors]))

(def themes
  {:light {:default    colors/primary-50
           :secondary  colors/black-opa-5
           :grey       colors/neutral-30}
   :dark  {:default    colors/primary-60
           :secondary  colors/white-opa-10
           :grey       colors/neutral-70}})

(defn get-color [key]
  (get-in themes [(theme/get-theme) key]))

(defn counter
  "type:    default, secondary, grey
   outline: true, false
   value:   integer"
  [{:keys [type outline]} value]
  (let [type       (or type :default)
        text-color (if (or
                        (= (theme/get-theme) :dark)
                        (and
                         (= type :default)
                         (not outline)))
                     colors/white
                     colors/black)
        value      (if (integer? value)
                     value
                     (js/parseInt value))
        label      (if (> value 99)
                     "99+"
                     (str value))
        width      (case (count label)
                     1     16
                     2     20
                     28)]
    [rn/view {:style (cond-> {:align-items     :center
                              :justify-content :center
                              :border-radius   6
                              :width           width
                              :height          16}
                       outline
                       (merge {:border-width 1
                               :border-color (get-color (or type :default))})

                       (not outline)
                       (assoc :background-color (get-color (or type :default)))

                       (> value 99)
                       (assoc :padding-left 0.5))}
     [text/text {:weight :medium
                 :size   :label
                 :style  {:color text-color}} label]]))
