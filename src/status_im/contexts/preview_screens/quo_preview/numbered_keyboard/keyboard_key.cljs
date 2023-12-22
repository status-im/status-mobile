(ns status-im.contexts.preview-screens.quo-preview.numbered-keyboard.keyboard-key
  (:require
    [quo.core :as quo]
    [reagent.core :as reagent]
    [status-im.contexts.preview-screens.quo-preview.preview :as preview]))

(def descriptor
  [{:key :blur? :type :boolean}
   {:key :disabled? :type :boolean}
   {:type    :select
    :key     :type
    :options [{:key :digit}
              {:key :key}
              {:key :derivation-path}]}])

(defn view
  []
  (let [state (reagent/atom {:disabled? false
                             :on-press  #(js/alert "pressed" %)
                             :blur?     false
                             :type      :digit})]
    (fn []
      (let [value (case (:type @state)
                    :key             :i/delete
                    :derivation-path nil
                    :digit           1
                    nil)]
        [preview/preview-container
         {:state                 state
          :descriptor            descriptor
          :blur?                 (:blur? @state)
          :show-blur-background? (:blur? @state)}
         [quo/keyboard-key @state value]]))))
