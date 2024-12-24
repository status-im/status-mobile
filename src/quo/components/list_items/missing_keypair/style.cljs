(ns quo.components.list-items.missing-keypair.style
  (:require
    [quo.foundations.colors :as colors]))

(defn container
  [{:keys [blur? theme]}]
  {:flex-direction   :row
   :align-items      :center
   :flex             1
   :padding-right    12
   :padding-left     8
   :padding-vertical 8
   :border-radius    12
   :border-width     (if blur? 0 1)
   :border-color     (colors/theme-colors colors/neutral-10
                                          colors/neutral-80
                                          theme)
   :background-color (if blur?
                       colors/white-opa-5
                       (colors/theme-colors colors/neutral-2_5
                                            colors/neutral-80-opa-40
                                            theme))})

(def name-container
  {:flex          1
   :padding-right 16
   :padding-left  8})

(def preview-list-container
  {:padding-right 16})

(defn options-icon-color
  [{:keys [theme blur?]}]
  (if blur?
    colors/white-opa-70
    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))
