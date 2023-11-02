(ns quo.components.dropdowns.dropdown-input.view
  (:require
    [quo.components.dropdowns.dropdown.properties :as properties]
    [quo.components.dropdowns.dropdown.style :as style]
    [quo.components.icon :as icon]
    [quo.components.markdown.text :as text]
    [quo.foundations.customization-colors :as customization-colors]
    [quo.theme :as theme]
    [react-native.blur :as blur]
    [react-native.core :as rn]))

(defn view-internal
  []
  [rn/pressable
   {:on-press nil}])

(def view
  "Props:
    - :on-press - function to call when pressed"
  (theme/with-theme view-internal))
