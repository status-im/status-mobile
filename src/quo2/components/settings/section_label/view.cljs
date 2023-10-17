(ns quo2.components.settings.section-label.view
  (:require
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [quo2.theme :as quo.theme]))

(defn- get-text-color
  [theme blur?]
  (if blur?
    colors/white-opa-40
    (colors/theme-colors colors/neutral-50
                         colors/neutral-40
                         theme)))

(defn label-style
  [color]
  {:color         color
   :margin-bottom 2})

(defn- view-internal
  "Props:
   - section - the label of the section
   - description (optional) - description of the section
   - blur? (optional) - use blurred styling
   - theme - light or dark"
  [{:keys [section description blur? theme]}]
  (let [color        (get-text-color theme (or blur? false))
        description? (not (nil? description))]
    [:<>
     [text/text
      {:number-of-lines 1
       :size            (if description? :paragraph-1 :paragraph-2)
       :weight          :medium
       :style           (label-style color)}
      section]
     (when description?
       [text/text
        {:size   :paragraph-1
         :weight :regular
         :style  {:color color}}
        description])]))

(def view (quo.theme/with-theme view-internal))
