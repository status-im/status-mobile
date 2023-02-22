(ns quo2.components.input.view
  (:require [quo2.components.icon :as icon]
            [quo2.components.input.style :as style]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

(def ^:private custom-props [:type :variant :error :right-icon])

(defn- base-input
  [_]
  (let [status (reagent/atom :default)]
    (fn [{:keys [variant error right-icon]
          :or   {variant :dark-blur}
          :as   props}]
      (let [colors-by-status (get-in style/status-colors [variant (if error :error @status)])
            clean-props      (apply dissoc props custom-props)]
        [rn/view
         [rn/text-input
          (merge {:style                  (style/input colors-by-status)
                  :placeholder-text-color (:placeholder-color colors-by-status)
                  :cursor-color           (:cursor-color colors-by-status)
                  :on-focus               #(reset! status :focus)
                  :on-blur                #(reset! status :default)}
                 clean-props)]
         (when-let [{:keys [on-press icon-name]} right-icon]
           [rn/touchable-opacity
            {:style    style/right-icon-touchable-area
             :on-press on-press}
            [icon/icon icon-name style/icon]])]))))

(defn- password-input
  [_]
  (let [password-shown? (reagent/atom false)]
    (fn [props]
      [base-input
       (assoc props
              :auto-capitalize   :none
              :auto-complete     :new-password
              :secure-text-entry (not @password-shown?)
              :right-icon        {:on-press  #(swap! password-shown? not)
                                  :icon-name (if @password-shown? :i/hide :i/reveal)})])))

(defn input
  "{:type      :password
    :variant   :dark-blur
    ...
    & any other TextInput available prop, such as: value, :default-value & :on-change
    }"
  [{:keys [type]
    ; TODO(@ulisesmac): Temp default type, should be removed as the component grows
    :or   {type :password}
    :as   props}]
  (if (= type :password)
    [password-input props]
    [rn/text "Not implemented"]))
