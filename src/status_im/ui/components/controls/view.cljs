(ns status-im.ui.components.controls.view
  (:require [cljs-bean.core :as bean]
            [react-native.reanimated :as animated]
            [status-im.ui.components.controls.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im.ui.components.icons.icons :as icons]))

(defn control-builder
  [component]
  (fn [props]
    (let [{:keys [value onChange disabled]}
          (bean/bean props)]
      (reagent/as-element
       [rn/touchable-without-feedback
        {:on-press (fn []
                     (when (and (not disabled) onChange)
                       (onChange (not value))))
         :enabled  (boolean (and onChange (not disabled)))}
        [rn/view
         [component
          {:disabled disabled}]]]))))

(defn switch-view
  [{:keys [transition hold disabled value]}]
  [animated/view
   {:style               (styles/switch-style transition disabled)
    :accessibility-label (str "switch-" (if value "on" "off"))
    :accessibility-role  :switch}
   [animated/view {:style (styles/switch-bullet-style transition hold)}]])

(defn radio-view
  [{:keys [transition hold disabled value]}]
  [animated/view
   {:style               (styles/radio-style transition disabled)
    :accessibility-label (str "radio-" (if value "on" "off"))
    :accessibility-role  :radio}
   [animated/view {:style (styles/radio-bullet-style transition hold)}]])

(defn checkbox-view
  [props]
  (let [{:keys [value onChange disabled]} (bean/bean props)]
    (reagent/as-element
     [rn/touchable-without-feedback
      {:on-press (when (and onChange (not disabled)) onChange)}
      [rn/view
       {:style               (styles/checkbox-style value disabled)
        :accessibility-label (str "checkbox-" (if value "on" "off"))
        :accessibility-role  :checkbox}
       [rn/view {:style (styles/check-icon-style value)}
        [icons/tiny-icon :tiny-icons/tiny-check {:color colors/white}]]]])))

(defn animated-checkbox-view
  [{:keys [transition hold disabled value]}]
  [animated/view
   {:style               (styles/animated-checkbox-style transition disabled)
    :accessibility-label (str "checkbox-" (if value "on" "off"))
    :accessibility-role  :checkbox}
   [animated/view {:style (styles/animated-check-icon-style transition hold)}
    [icons/tiny-icon :tiny-icons/tiny-check {:color colors/white}]]])

(def switch (reagent/adapt-react-class (control-builder switch-view)))
(def radio (reagent/adapt-react-class (control-builder radio-view)))
(def animated-checkbox
  (reagent/adapt-react-class (control-builder animated-checkbox-view)))
(def checkbox (reagent/adapt-react-class checkbox-view))
