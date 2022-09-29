(ns quo2.components.counter.counter
  (:require [quo.theme :as theme]
            [quo.react-native :as rn]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]))

(def themes
  {:light {:default    colors/primary-50
           :secondary  colors/neutral-80-opa-5
           :grey       colors/neutral-10
           :outline    colors/neutral-20}
   :dark  {:default    colors/primary-60
           :secondary  colors/white-opa-5
           :grey       colors/neutral-70
           :outline    colors/neutral-70}})

(defn get-color [key]
  (get-in themes [(theme/get-theme) key]))

(defn counter
  "type:    default, secondary, grey, outline
   value:   integer"
  [{:keys [type override-text-color override-bg-color style]} value]
  (let [type       (or type :default)
        text-color (or override-text-color
                       (if (or
                            (= (theme/get-theme) :dark)
                            (= type :default))
                         colors/white
                         colors/neutral-100))
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
    [rn/view {:style (cond-> (merge
                              {:align-items     :center
                               :justify-content :center
                               :border-radius   6
                               :width           width
                               :height          16}
                              style)
                       (= type :outline)
                       (merge {:border-width 1
                               :border-color (get-color type)})

                       (not= type :outline)
                       (assoc :background-color
                              (or override-bg-color
                                  (get-color type)))

                       (> value 99)
                       (assoc :padding-left 0.5))}
     [text/text {:weight :medium
                 :size   :label
                 :style  {:color text-color}} label]]))
