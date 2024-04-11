(ns quo.components.wallet.amount-input.view
  (:require
    [quo.components.buttons.button.view :as button]
    [quo.components.markdown.text :as text]
    [quo.components.wallet.amount-input.schema :as amount-input.schema]
    [quo.components.wallet.amount-input.style :as style]
    [quo.theme]
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

(defn- view-internal
  [{:keys [on-inc-press on-dec-press status value min-value max-value
           container-style]
    :or   {value     0
           min-value 0
           max-value 999999999}}]
  (let [theme (quo.theme/use-theme-value)]
    [rn/view
     {:style (merge style/container container-style)}
     [amount-button
      {:theme               theme
       :accessibility-label :amount-input-dec-button
       :icon                :i/remove
       :on-press            on-dec-press
       :disabled?           (>= min-value value)}]
     [rn/view {:style style/input-container}
      [text/text
       {:number-of-lines     1
        :accessibility-label :amount-input
        :weight              :semi-bold
        :size                :heading-1
        :align-self          :center
        :style               (style/input-text theme (or status :default))}
       value]]
     [amount-button
      {:theme               theme
       :icon                :i/add
       :accessibility-label :amount-input-inc-button
       :on-press            on-inc-press
       :disabled?           (>= value max-value)}]]))

(def view (schema/instrument #'view-internal amount-input.schema/?schema))
