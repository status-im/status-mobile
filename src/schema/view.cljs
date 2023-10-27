(ns schema.view
  (:require
    [react-native.core :as rn]
    schema.state))

(defn view
  []
  (let [on-press #(reset! schema.state/errors #{})]
    (fn []
      (when (seq @schema.state/errors)
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
