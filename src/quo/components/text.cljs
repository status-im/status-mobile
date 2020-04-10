(ns quo.components.text
  (:require [quo.animated :as animated]
            [quo.design-system.colors :as colors]
            [quo.design-system.typography :as typography]
            [quo.react-native :as rn]
            [reagent.core :as reagent]))

(defn text-style [{:keys [size align weight color style]
                   :or   {size   :base
                          weight :regular
                          align  :auto
                          color  :main}}]
  (merge (case weight
           :regular   typography/font-regular
           :medium    typography/font-medium
           :semi-bold typography/font-semi-bold
           :bold      typography/font-bold
           :monospace typography/monospace
           :inherit   nil)
         (case color
           :main              {:color (:text-01 @colors/theme)}
           :secondary         {:color (:text-02 @colors/theme)}
           :secondary-inverse {:color (:text-03 @colors/theme)}
           :link              {:color (:text-04 @colors/theme)}
           :positive          {:color (:positive-01 @colors/theme)}
           :negative          {:color (:negative-01 @colors/theme)}
           :inherit           nil)
         (case size
           :tiny     typography/tiny
           :small    typography/small
           :base     typography/base
           :large    typography/large
           :x-large  typography/x-large
           :xx-large typography/xx-large
           :inherit  nil)
         {:text-align align}
         style))

(defn text []
  (let [this      (reagent/current-component)
        props     (reagent/props this)
        component (if (:animated? props) animated/text rn/text)]
    (into [component (merge {:style (text-style props)}
                            (dissoc props
                                    :style :size :weight :color
                                    :align :animated?))]
          (reagent/children this))))
