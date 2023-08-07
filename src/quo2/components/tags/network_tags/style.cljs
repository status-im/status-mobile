(ns quo2.components.tags.network-tags.style
  (:require [quo2.foundations.colors :as colors]))

(defn network-tags-container
  [status]
  {:flex-direction   :row
   :background-color (if (= status :error)
                       colors/danger-50-opa-10
                       {})
   :border-width     2
   :border-color     (if (= status :error)
                       colors/danger-50-opa-20
                       (colors/theme-colors
                        colors/neutral-20
                        colors/neutral-80))
   :border-radius    12
   :align-items      :center
   :padding-left     10
   :padding-vertical 6
   :justify-content  :flex-end})

(defn network-tag-view
  []
  {:flex-direction  :row
   :align-items     :center
   :padding         5
   :justify-content :flex-end})

(defn network-tag-title-style
  [status]
  {:weight :medium
   :style  (if (= status :error)
             {:color colors/danger-50}
             {})
   :size   :paragraph-2})
