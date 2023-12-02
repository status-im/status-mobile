(ns quo.components.dropdowns.dropdown-input.properties
  (:require
    [quo.foundations.colors :as colors]))

(def icon-size 20)

(defn- grey-blur
  [theme active?]
  {:left-icon-color    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-70 theme)
   :right-icon-color   (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)
   :right-icon-color-2 (colors/theme-colors colors/neutral-100 colors/white theme)
   :label-color        (colors/theme-colors colors/neutral-100 colors/white theme)
   :border-color       (if active?
                         (colors/theme-colors colors/neutral-80-opa-20
                                              colors/white-opa-40
                                              theme)
                         (colors/theme-colors colors/neutral-80-opa-10
                                              colors/white-opa-10
                                              theme))})

(defn- outline
  [theme active?]
  {:left-icon-color    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
   :right-icon-color   (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :right-icon-color-2 (colors/theme-colors colors/neutral-100 colors/white theme)
   :label-color        (colors/theme-colors colors/neutral-100 colors/white theme)
   :border-color       (if active?
                         (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)
                         (colors/theme-colors colors/neutral-30 colors/neutral-70 theme))})

(defn- grey
  [theme active?]
  {:left-icon-color    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
   :right-icon-color   (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :right-icon-color-2 (colors/theme-colors colors/neutral-100 colors/white theme)
   :label-color        (colors/theme-colors colors/neutral-100 colors/white theme)
   :border-color       (if active?
                         (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)
                         (colors/theme-colors colors/neutral-20
                                              colors/neutral-60
                                              theme))})

(defn get-colors
  [{:keys [theme state blur?]}]
  (let [active? (= state :active)]
    (cond
      blur?            (grey-blur theme active?)
      (= theme :dark)  (outline theme active?)
      (= theme :light) (grey theme active?))))
