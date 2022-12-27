(ns quo2.components.markdown.text
  (:require [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [reagent.core :as reagent]))

(defn text-style
  [{:keys [size align weight style]}]
  (merge (case (or weight :regular)
           :regular   typography/font-regular
           :medium    typography/font-medium
           :semi-bold typography/font-semi-bold
           :bold      typography/font-bold
           :monospace typography/monospace
           :code      typography/code
           :inherit   nil)
         (case (or size :paragraph-1)
           :label       typography/label
           :paragraph-2 typography/paragraph-2
           :paragraph-1 typography/paragraph-1
           :heading-2   typography/heading-2
           :heading-1   typography/heading-1
           :inherit     nil)
         {:text-align (or align :auto)}
         (if (:color style)
           style
           (assoc style :color (if (= (theme/get-theme) :dark) colors/white colors/neutral-100)))))

(defn text
  []
  (let [this  (reagent/current-component)
        props (reagent/props this)
        style (text-style props)]
    (into [rn/text
           (merge {:style style}
                  (dissoc props :style :size :align :weight :color))]
          (reagent/children this))))
