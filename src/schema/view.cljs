(ns schema.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    schema.state
    [schema.style :as style]))

(defn view
  []
  (let [on-press #(reset! schema.state/errors #{})]
    (fn []
      (when (seq @schema.state/errors)
        [rn/pressable
         {:on-press on-press
          :style    (style/container {:bottom-inset (safe-area/get-bottom)})}
         [quo/icon :i/close {:size 12 :color "#ddd" :container-style style/icon}]
         [rn/text {:style style/text}
          "Schema error(s)"
          [rn/text {:style (merge style/text style/text-suffix)} " check logs"]]]))))
