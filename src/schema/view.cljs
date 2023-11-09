(ns schema.view
  (:require
    [react-native.core :as rn]
    schema.state
    [schema.style :as style]))

(defn view
  []
  (let [on-press #(reset! schema.state/errors #{})]
    (fn []
      (when (seq @schema.state/errors)
        [rn/pressable {:on-press on-press :style style/container}
         [rn/text {:style style/text}
          "Schema error"]]))))
