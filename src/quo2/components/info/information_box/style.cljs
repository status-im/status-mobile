(ns quo2.components.info.information-box.style
  (:require [quo2.foundations.colors :as colors]))

(def ^:private themes
  {:light {:default      {:bg     colors/white
                          :border colors/neutral-20
                          :icon   colors/neutral-50
                          :text   colors/neutral-100}
           :informative  {:bg     colors/primary-50-opa-5
                          :border colors/primary-50-opa-10
                          :icon   colors/primary-50
                          :text   colors/neutral-100}
           :error        {:bg     colors/danger-50-opa-5
                          :border colors/danger-50-opa-10
                          :icon   colors/danger-50
                          :text   colors/danger-50}
           :close-button colors/neutral-100}
   :dark  {:default      {:bg     colors/neutral-90
                          :border colors/neutral-70
                          :icon   colors/neutral-40
                          :text   colors/white}
           :informative  {:bg     colors/primary-50-opa-5
                          :border colors/primary-50-opa-10
                          :icon   colors/white
                          :text   colors/white}
           :error        {:bg     colors/danger-50-opa-5
                          :border colors/danger-50-opa-10
                          :icon   colors/danger-50
                          :text   colors/danger-50}
           :close-button colors/white}})

(defn get-color
  [theme k]
  (get-in themes [theme k]))

(defn get-color-by-type
  [theme type k]
  (get-in themes [theme type k]))

(defn container
  [{:keys [theme type include-button?]}]
  {:background-color   (get-color-by-type theme type :bg)
   :border-color       (get-color-by-type theme type :border)
   :border-width       1
   :border-radius      12
   :padding-top        (if include-button? 10 11)
   :padding-bottom     (if include-button? 12 11)
   :flex-direction     :row
   :padding-horizontal 16})

(def icon
  {:margin-top 1 :margin-right 8})

(def close-button
  {:margin-top  4
   :margin-left 8})

(defn content-text
  [theme type]
  {:color        (get-color-by-type theme type :text)
   :margin-right 8})

(def content-button
  {:margin-top 8
   :align-self :flex-start})
