(ns quo.components.wallet.amount-input.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.markdown.text :as text]
    [quo.components.wallet.amount-input.schema :as amount-input.schema]
    [quo.components.wallet.amount-input.style :as style]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
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
  [{:keys [init-value status min-value max-value auto-focus? return-key-type container-style
           on-change-text]}]
  (let [theme             (quo.theme/use-theme-value)
        [value set-value] (rn/use-state (or init-value 0))
        on-dec-press      #(set-value (dec value))
        on-inc-press      #(set-value (inc value))
        min-value         (or min-value 0)
        max-value         (or max-value 999999999)
        on-change-text    (rn/use-callback
                           (fn [input-value]
                             (let [processed-amount (process-amount input-value min-value max-value)]
                               (set-value processed-amount)
                               (when on-change-text
                                 (on-change-text processed-amount))))
                           [min-value max-value on-change-text])]
    [rn/view
     {:style (merge style/container container-style)}
     [amount-button
      {:theme               theme
       :accessibility-label :amount-input-dec-button
       :icon                :i/remove
       :on-press            on-dec-press
       :disabled?           (>= min-value value)}]
     [rn/view {:style style/input-container}
      [rn/text-input
       {:style               (text/text-style
                              {:size   :heading-1
                               :weight :semi-bold
                               :align  :center
                               :style  (style/input-text theme (or status :default))})
        :accessibility-label :amount-input
        :editable            true
        :auto-focus          (or auto-focus? false)
        :value               (str value)
        :keyboard-appearance (quo.theme/theme-value :light :dark theme)
        :return-key-type     (or return-key-type :done)
        :input-mode          :numeric
        :on-change-text      on-change-text}]]
     [amount-button
      {:theme               theme
       :icon                :i/add
       :accessibility-label :amount-input-inc-button
       :on-press            on-inc-press
       :disabled?           (>= value max-value)}]]))

(def view (schema/instrument #'view-internal amount-input.schema/?schema))
