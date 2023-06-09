(ns quo2.components.counter.counter
  (:require
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as theme]
    [react-native.core :as rn]
    [utils.number]))

(def themes
  {:light {:default   colors/primary-50
           :secondary colors/neutral-80-opa-5
           :grey      colors/neutral-10
           :outline   colors/neutral-20}
   :dark  {:default   colors/primary-60
           :secondary colors/white-opa-5
           :grey      colors/neutral-70
           :outline   colors/neutral-70}})

(defn get-color
  [key]
  (get-in themes [(theme/get-theme) key]))

(defn counter
  [{:keys [type override-text-color override-bg-color style accessibility-label max-value]
    :or   {max-value 99}}
   value]
  (let [type       (or type :default)
        text-color (or override-text-color
                       (if (or (= (theme/get-theme) :dark)
                               (= type :default))
                         colors/white
                         colors/neutral-100))
        value      (utils.number/parse-int value)
        label      (if (> value max-value)
                     (str max-value "+")
                     (str value))
        width      (case (count label)
                     1 16
                     2 20
                     28)]
    [rn/view
     {:test-ID             :counter-component
      :accessible          true
      :accessibility-label accessibility-label
      :style               (cond-> (merge
                                    {:align-items     :center
                                     :justify-content :center
                                     :border-radius   6
                                     :width           width
                                     :height          16}
                                    style)
                             (= type :outline)
                             (merge {:border-width 1
                                     :border-color (colors/get-color :yin-yang (theme/get-theme))})

                             (not= type :outline)
                             (assoc :background-color
                                    (or override-bg-color
                                        (colors/get-color :yin-yang (theme/get-theme))))

                             (> value max-value)
                             (assoc :padding-left 0.5))}
     [text/text
      {:weight :medium
       :size   :label
       :style  {:color (colors/get-secondary-color
                        :yin-yang
                        (theme/get-theme)
                        colors/white)}}
      label]]))
