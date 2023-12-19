(ns legacy.status-im.ui.components.text
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.typography :as typography]
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(defn text-style
  [{:keys [size align weight monospace color style]}]
  ;; NOTE(Ferossgo): or in destructoring will keep nil as a value
  (merge (if monospace
           ;; TODO(Ferossgp): Add all weights for monospace
           typography/monospace
           (case (or weight :regular)
             :regular   typography/font-regular
             :medium    typography/font-medium
             :semi-bold typography/font-semi-bold
             :bold      typography/font-bold
             :monospace typography/monospace ; DEPRECATED
             :inherit   nil))
         (case (or color :main)
           :main              {:color (:text-01 @colors/theme)}
           :secondary         {:color (:text-02 @colors/theme)}
           :secondary-inverse {:color (:text-03 @colors/theme)}
           :link              {:color (:text-04 @colors/theme)}
           :inverse           {:color (:text-05 @colors/theme)}
           :positive          {:color (:positive-01 @colors/theme)}
           :negative          {:color (:negative-01 @colors/theme)}
           :inherit           nil)
         (case (or size :base)
           :tiny     typography/tiny
           :x-small  typography/x-small
           :small    typography/small
           :base     typography/base
           :large    typography/large
           :x-large  typography/x-large
           :xx-large typography/xx-large
           :inherit  nil)
         {:text-align (or align :auto)}
         style))

(defn text
  []
  (let [this      (reagent/current-component)
        props     (reagent/props this)
        component rn/text]
    (into [component
           (merge {:style (text-style props)}
                  (dissoc props
                   :style
                   :size
                   :weight
                   :color
                   :align
                   :animated?))]
          (reagent/children this))))
