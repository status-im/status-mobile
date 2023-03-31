(ns quo2.components.selectors.selectors.view
  (:require [quo2.components.icon :as icons]
            [quo2.components.selectors.selectors.style :as style]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

(defn- handle-press
  [on-change checked-atom checked?]
  (when checked-atom (swap! checked-atom not))
  (when on-change (on-change (not checked?))))

(defn- selector
  [{:keys [default-checked? checked?]}]
  (let [controlled-component? (some? checked?)
        internal-checked?     (when-not controlled-component?
                                (reagent/atom (or default-checked? false)))]
    (fn [{:keys [checked? disabled? blur? customization-color on-change container-style
                 label-prefix outer-style-fn inner-style-fn icon-style-fn]
          :or   {customization-color :blue}}]
      (let [actual-checked?     (if controlled-component? checked? @internal-checked?)
            accessibility-label (str label-prefix "-" (if actual-checked? "on" "off"))
            test-id             (str label-prefix "-component")
            outer-styles        (outer-style-fn {:checked?            actual-checked?
                                                 :disabled?           disabled?
                                                 :blur?               blur?
                                                 :container-style     container-style
                                                 :customization-color customization-color})]
        [rn/touchable-without-feedback
         (when-not disabled?
           {:on-press #(handle-press on-change internal-checked? actual-checked?)})
         [rn/view
          {:style               outer-styles
           :accessibility-label accessibility-label
           :accessibility-role  :checkbox
           :testID              test-id}
          [rn/view
           {:style (inner-style-fn {:checked?            actual-checked?
                                    :blur?               blur?
                                    :customization-color customization-color})}
           (when (and icon-style-fn actual-checked?)
             [icons/icon :i/check-small (icon-style-fn actual-checked? blur?)])]]]))))

(defn toggle
  [props]
  [selector
   (assoc props
          :label-prefix   "toggle"
          :outer-style-fn style/toggle
          :inner-style-fn style/toggle-inner)])

(defn radio
  [props]
  [selector
   (assoc props
          :label-prefix   "radio"
          :outer-style-fn style/radio
          :inner-style-fn style/radio-inner)])

(defn checkbox
  [props]
  [selector
   (assoc props
          :label-prefix   "checkbox"
          :outer-style-fn style/checkbox
          :inner-style-fn style/common-checkbox-inner
          :icon-style-fn  style/checkbox-check)])

(defn checkbox-prefill
  [props]
  [selector
   (assoc props
          :label-prefix   "checkbox-prefill"
          :outer-style-fn style/checkbox-prefill
          :inner-style-fn style/common-checkbox-inner
          :icon-style-fn  style/checkbox-prefill-check)])
