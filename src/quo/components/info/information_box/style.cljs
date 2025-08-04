(ns quo.components.info.information-box.style
  (:require
    [quo.foundations.colors :as colors]))

(defn get-colors-map
  [customization-color-hex]
  {:light {:default      {:bg     colors/neutral-5
                          :border colors/neutral-20
                          :icon   colors/neutral-50
                          :text   colors/neutral-100}
           :informative  {:bg     (colors/alpha customization-color-hex 0.05)
                          :border (colors/alpha customization-color-hex 0.1)
                          :icon   customization-color-hex
                          :text   colors/neutral-100}
           :error        {:bg     colors/danger-50-opa-5
                          :border colors/danger-50-opa-10
                          :icon   colors/danger-50
                          :text   colors/danger-50}
           :close-button colors/neutral-100}
   :dark  {:default      {:bg     colors/neutral-80-opa-40
                          :border colors/neutral-80
                          :icon   colors/neutral-40
                          :text   colors/white}
           :informative  {:bg     (colors/alpha customization-color-hex 0.05)
                          :border (colors/alpha customization-color-hex 0.1)
                          :icon   customization-color-hex
                          :text   colors/white}
           :error        {:bg     colors/danger-60-opa-5
                          :border colors/danger-60-opa-10
                          :icon   colors/danger-60
                          :text   colors/danger-60}
           :close-button colors/white}
   :shell {:default      {:bg     colors/white-opa-5
                          :border colors/white-opa-10
                          :icon   colors/white-opa-70
                          :text   colors/white}
           :informative  {:bg     (colors/alpha customization-color-hex 0.05)
                          :border (colors/alpha customization-color-hex 0.1)
                          :icon   customization-color-hex
                          :text   colors/white}
           :error        {:bg     colors/danger-60-opa-5
                          :border colors/danger-60-opa-10
                          :icon   colors/danger-60
                          :text   colors/danger-60}
           :close-button colors/white}})

(defn get-color
  [colors-map theme k]
  (get-in colors-map [theme k]))

(defn get-color-by-type
  [colors-map theme type k]
  (get-in colors-map [theme type k]))

(defn container
  [{:keys [theme type include-button? colors-map]}]
  {:background-color   (get-color-by-type colors-map theme type :bg)
   :border-color       (get-color-by-type colors-map theme type :border)
   :border-width       1
   :border-radius      12
   :padding-top        (if include-button? 10 11)
   :padding-bottom     (if include-button? 12 11)
   :flex-direction     :row
   :padding-horizontal 12})

(def icon
  {:margin-top 1 :margin-right 8})

(def close-button
  {:margin-top  4
   :margin-left 8})

(defn content-text
  [theme type colors-map]
  {:color        (get-color-by-type colors-map theme type :text)
   :margin-right 8})

(def content-button
  {:margin-top 8
   :align-self :flex-start})
