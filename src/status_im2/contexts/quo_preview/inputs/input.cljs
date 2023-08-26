(ns status-im2.contexts.quo-preview.inputs.input
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [reagent.core :as reagent]
            [status-im2.contexts.quo-preview.preview :as preview]))

(def descriptor
  [{:key     :type
    :type    :select
    :options [{:key :text}
              {:key :password}]}
   {:key  :blur?
    :type :boolean}
   {:key  :error?
    :type :boolean}
   {:key  :icon-name
    :type :boolean}
   {:key  :disabled?
    :type :boolean}
   {:key  :clearable?
    :type :boolean}
   {:key  :small?
    :type :boolean}
   {:key  :multiline
    :type :boolean}
   {:key  :button
    :type :boolean}
   {:key  :label
    :type :text}
   {:key     :char-limit
    :type    :select
    :options [{:key   10
               :value "10"}
              {:key   50
               :value "50"}
              {:key   100
               :value "100"}]}
   {:key  :value
    :type :text}])

(defn view
  []
  (let [state (reagent/atom {:type                :text
                             :blur?               false
                             :placeholder         "Type something"
                             :error               false
                             :icon-name           false
                             :value               ""
                             :clearable           false
                             :on-char-limit-reach #(js/alert
                                                    (str "Char limit reached: " %))})]
    (fn []
      (let [blank-label? (string/blank? (:label @state))]
        [preview/preview-container
         {:state                 state
          :descriptor            descriptor
          :blur?                 (:blur? @state)
          :show-blur-background? true}
         [quo/input
          (cond-> (assoc @state
                         :on-clear?      #(swap! state assoc :value "")
                         :on-change-text #(swap! state assoc :value %))
            (:button @state)
            (assoc :button
                   {:on-press #(js/alert "Button pressed!")
                    :text     "My button"})

            blank-label?
            (dissoc :label)

            (:icon-name @state)
            (assoc :icon-name :i/placeholder))]]))))
