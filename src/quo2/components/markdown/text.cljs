(ns quo2.components.markdown.text
  (:require [quo.react-native :as rn]
            [quo2.reanimated :as reanimated]
            [reagent.core :as reagent]
            [quo.theme :as theme]
            [quo2.foundations.colors :as colors]
            [quo2.foundations.typography :as typography]))

(defn text-style [{:keys [size align weight style]}]
  (merge
   style
   (case (or weight :regular)
     :regular typography/font-regular
     :medium typography/font-medium
     :semi-bold typography/font-semi-bold
     :bold typography/font-bold
     :monospace typography/monospace
     :inherit nil)
   (if (number? size) {:font-size   size}
       (case (or size :paragraph-1)
         :label typography/label
         :paragraph-2 typography/paragraph-2
         :paragraph-1 typography/paragraph-1
         :heading-2 typography/heading-2
         :heading-1 typography/heading-1
         :inherit nil))
   {:text-align (or align :auto)}
   (when-not (:color style)
     {:color (if (= (theme/get-theme) :dark) colors/white colors/black)})))

(defn text []
  (let [this  (reagent/current-component)
        props (reagent/props this)
        children (reagent/children this)
        style (text-style props)]
    [:f>
     (fn []
       (into
        (if (:animated-style props)
          (let [props-without-styles (dissoc props :style :animated-style :size :align :weight :color)
                merged-style (reanimated/apply-animations-to-style (:animated-style props) style)]
            [reanimated/text (merge {:style merged-style}
                                    props-without-styles)])
          (let [props-without-styles (dissoc props :style :size :align :weight :color)]
            [rn/text (merge {:style style}
                            props-without-styles)]))
        children))]))