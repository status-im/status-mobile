(ns quo.components.inputs.amount-input.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.inputs.amount-input.schema :as amount-input.schema]
    [quo.components.inputs.amount-input.style :as style]
    [quo.components.markdown.text :as text]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [schema.core :as schema]))

(defn- amount-button
  [{:keys [theme accessibility-label disabled? icon on-press]}]
  [button/button
   {:icon-only?          true
    :theme               theme
    :disabled?           disabled?
    :type                :outline
    :accessibility-label accessibility-label
    :size                32
    :on-press            on-press}
   icon])

(defn- process-amount
  [input-value min-value max-value]
  (let [parsed-input-value (parse-double input-value)]
    (cond
      (nil? parsed-input-value)  min-value
      (>= input-value max-value) max-value
      (<= input-value min-value) min-value
      :else                      parsed-input-value)))

(defn- view-internal
  [{:keys [on-change-text
           auto-focus
           init-value
           return-key-type
           container-style]
    :or   {auto-focus false
           init-value 0}}]
  (let [value (reagent/atom init-value)]
    (fn [{:keys [customization-color theme status min-value max-value]
          :or   {status    :default
                 min-value 0
                 max-value 999999999}}]
      [rn/view
       {:style (merge style/container container-style)}
       [amount-button
        {:theme               theme
         :accessibility-label :amount-input-dec-button
         :icon                :i/remove
         :on-press            #(swap! value dec)
         :disabled?           (>= min-value @value)}]
       [rn/view {:style style/input-container}
        [rn/text-input
         {:style
          (text/text-style
           {:size   :heading-1
            :weight :semi-bold
            :align  :center
            :style  (style/input-text theme status)})
          :auto-capitalize :none
          :keyboard-type :numeric
          :accessibility-label :amount-input
          :editable true
          :auto-focus auto-focus
          :value (str @value)
          :keyboard-appearance (quo.theme/theme-value :light :dark theme)
          :return-key-type return-key-type
          :input-mode :numeric
          :on-change-text (fn [input-value]
                            (let [processed-amount (process-amount input-value min-value max-value)]
                              (reset! value processed-amount)
                              (when on-change-text
                                (on-change-text processed-amount))))
          :selection-color (style/get-selection-color customization-color theme)}]]
       [amount-button
        {:theme               theme
         :icon                :i/add
         :accessibility-label :amount-input-inc-button
         :on-press            #(swap! value inc)
         :disabled?           (>= @value max-value)}]])))

(def view
  (quo.theme/with-theme
   (schema/instrument #'view-internal amount-input.schema/?schema)))
