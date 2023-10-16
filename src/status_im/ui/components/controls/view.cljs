(ns status-im.ui.components.controls.view
  (:require [status-im.ui.components.controls.styles :as styles]
            [status-im.ui.components.colors :as colors]
            [react-native.core :as rn]
            [status-im.ui.components.icons.icons :as icons]))

(defn switch
  [{:keys [disabled value]}]
  [rn/view
   {:style               (styles/switch-style value disabled)
    :accessibility-label (str "switch-" (if value "on" "off"))
    :accessibility-role  :switch}
   [rn/view {:style (styles/switch-bullet-style value false)}]])

(defn radio
  [{:keys [disabled value]}]
  [rn/view
   {:style               (styles/radio-style value disabled)
    :accessibility-label (str "radio-" (if value "on" "off"))
    :accessibility-role  :radio}
   [rn/view {:style (styles/radio-bullet-style value false)}]])

(defn checkbox
  [{:keys [value disabled]}]
  [rn/view
   {:style               (styles/checkbox-style value disabled)
    :accessibility-label (str "checkbox-" (if value "on" "off"))
    :accessibility-role  :checkbox}
   [rn/view {:style (styles/check-icon-style value)}
    [icons/tiny-icon :tiny-icons/tiny-check {:color colors/white}]]])
