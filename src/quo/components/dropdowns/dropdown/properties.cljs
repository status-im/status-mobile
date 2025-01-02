(ns quo.components.dropdowns.dropdown.properties
  (:require
    [quo.foundations.colors :as colors]))

(def backgrounds #{:photo :blur})

(def sizes
  {:size-40 {:size             40
             :icon-size        20
             :emoji-size       20
             :emoji-padding    10
             :padding-vertical 9
             :padding-left     {:default 16 :icon 12}
             :padding-right    12
             :border-radius    12
             :text-size        :paragraph-1}
   :size-32 {:size             32
             :icon-size        20
             :emoji-size       20
             :emoji-padding    6
             :padding-vertical 5
             :padding-left     {:default 12 :icon 8}
             :padding-right    8
             :border-radius    10
             :text-size        :paragraph-1}
   :size-24 {:size             24
             :icon-size        12
             :emoji-size       12
             :emoji-padding    6
             :padding-vertical 3
             :padding-left     {:default 8 :icon 8}
             :padding-right    8
             :border-radius    8
             :text-size        :paragraph-2}})

(defn- custom-color-type
  [customization-color]
  {:left-icon-color    colors/white-opa-70
   :right-icon-color   colors/white-opa-20
   :right-icon-color-2 colors/white
   :label-color        colors/white
   :background-color   (colors/custom-color customization-color 50)})

(def sizes-to-exclude-blur-in-photo-bg #{:size-40})

(defn- grey-photo
  [theme active? size]
  {:left-icon-color    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme)
   :right-icon-color   (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)
   :right-icon-color-2 (colors/theme-colors colors/neutral-100 colors/white theme)
   :label-color        (colors/theme-colors colors/neutral-100 colors/white theme)
   :blur-overlay-color (when (and (= theme :dark) (nil? (sizes-to-exclude-blur-in-photo-bg size)))
                         (if active? colors/neutral-80-opa-50 colors/neutral-80-opa-40))
   :blur-type          (if (= theme :light) :light :dark)
   :background-color   (cond
                         (= theme :light)
                         (if active? colors/white-opa-50 colors/white-opa-40)

                         (and (= theme :dark) (sizes-to-exclude-blur-in-photo-bg size))
                         (if active? colors/neutral-80-opa-50 colors/neutral-80-opa-40))})

(defn- grey-blur
  [theme active?]
  {:left-icon-color    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-70 theme)
   :right-icon-color   (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)
   :right-icon-color-2 (colors/theme-colors colors/neutral-100 colors/white theme)
   :label-color        (colors/theme-colors colors/neutral-100 colors/white theme)
   :background-color   (if active?
                         (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)
                         (colors/theme-colors colors/neutral-80-opa-5 colors/white-opa-5 theme))})

(defn- outline-blur
  [theme active?]
  {:left-icon-color    (colors/theme-colors colors/neutral-80-opa-40 colors/white-opa-40 theme)
   :right-icon-color   (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme)
   :right-icon-color-2 (colors/theme-colors colors/neutral-100 colors/white theme)
   :label-color        (colors/theme-colors colors/neutral-100 colors/white theme)
   :border-color       (if active?
                         (colors/theme-colors colors/neutral-80-opa-20 colors/white-opa-20 theme)
                         (colors/theme-colors colors/neutral-80-opa-10 colors/white-opa-10 theme))})

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
   :background-color   (if active?
                         (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
                         (colors/theme-colors colors/neutral-5 colors/neutral-90 theme))})

(defn- ghost
  [theme active?]
  {:left-icon-color    (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)
   :right-icon-color   (colors/theme-colors colors/neutral-20 colors/neutral-80 theme)
   :right-icon-color-2 (colors/theme-colors colors/neutral-100 colors/white theme)
   :label-color        (colors/theme-colors colors/neutral-100 colors/white theme)
   :background-color   (when active?
                         (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))})

(defn get-colors
  [{:keys [customization-color type state background size]} theme]
  (let [active? (= state :active)]
    (cond
      (and (= background :photo) (= type :grey))   (grey-photo theme active? size)
      (and (= background :blur) (= type :grey))    (grey-blur theme active?)
      (and (= background :blur) (= type :outline)) (outline-blur theme active?)
      (= type :outline)                            (outline theme active?)
      (= type :grey)                               (grey theme active?)
      (= type :ghost)                              (ghost theme active?)
      (= type :customization)                      (custom-color-type customization-color))))
