(ns quo.components.settings.section-label.view
  (:require
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- get-text-color
  [theme blur?]
  (if blur?
    colors/white-opa-40
    (colors/theme-colors colors/neutral-50
                         colors/neutral-40
                         theme)))

(defn label-style
  [color description?]
  (cond-> {:color color}

    description?
    (assoc :margin-bottom 2)))

(defn view
  "Props:
   - section - the label of the section
   - description (optional) - description of the section
   - blur? (optional) - use blurred styling
   - theme - light or dark"
  [{:keys [section description blur? container-style]}]
  (let [theme        (quo.theme/use-theme)
        color        (get-text-color theme (or blur? false))
        description? (not (nil? description))
        root-view    (if (seq container-style) rn/view :<>)]
    [root-view {:style container-style}
     [text/text
      {:number-of-lines 1
       :size            (if description? :paragraph-1 :paragraph-2)
       :weight          :medium
       :style           (label-style color description?)}
      section]
     (when description?
       [text/text
        {:size   :paragraph-1
         :weight :regular
         :style  {:color color}}
        description])]))
