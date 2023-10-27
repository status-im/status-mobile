(ns schema.ui
  (:require
    [react-native.core :as rn]
    [reagent.core :as reagent]))

(def schema-errors
  (reagent/atom #{}))

(defn view
  []
  (let [on-press #(reset! schema-errors #{})]
    (fn []
      (when (seq @schema-errors)
        [rn/pressable
         {:on-press on-press
          :style    {:position                  :absolute
                     :right                     0
                     :bottom                    0
                     :border-top-left-radius    8
                     :border-bottom-left-radius 8
                     :justify-content           :center
                     :align-items               :center
                     :padding                   6
                     :padding-horizontal        16
                     :background-color          "#cc0000"
                     :z-index                   10000000}}
         [rn/text
          {:style {:font-family "Inter-SemiBold"
                   :font-size   13
                   :color       "#dddddd"}}
          "Schema error"]]))))
