(ns quo2.components.settings.section-label.view
  (:require
    [quo2.theme :as quo.theme]
    [quo2.components.settings.section-label.style :as style]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]))

(defn- get-text-color
  [theme blur?]
  (if blur?
    colors/white-opa-40
    (colors/theme-colors colors/neutral-50
                         colors/neutral-40
                         theme)))

(defn view
  "Props:
   - section - the label of the section
   - description (optional) - description of the section
   - blur? (optional) - use blurred styling"
  [{:keys [section description blur?]}]
  (let [theme        (quo2.theme/get-theme)
        color        (get-text-color theme (or blur? false))
        description? (not (nil? description))]
    [:<>
     [rn/text
      {:number-of-lines 1
       :style           (style/section color
                                       (if description?
                                         :medium
                                         :small))}
      section]
     (when description?
       [rn/text {:style (style/description color)}
        description])]))
