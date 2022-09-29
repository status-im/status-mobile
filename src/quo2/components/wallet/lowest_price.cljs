(ns quo2.components.wallet.lowest-price
  (:require [quo.react-native :as rn]
            [clojure.string :as string]))

(def centrify {:style {:flex-direction  :row
                       :justify-content :center
                       :align-items     :center}})

(defn border-alignment [value-bg-color]
  {:style {:align-self         :flex-start
           :padding-horizontal 16
           :padding-vertical   2
           :flex-grow          0.25
           :border-radius      4
           :background-color   value-bg-color}})

(def line-dots-props
  {:ellipsize-mode  :middle
   :number-of-lines 1
   :font-size       10
   :line-height     2})

(defn dots
  "Returns the dots"
  [few-of-dots?]
  (let [dots-text (->> ". "
                       (repeat (if few-of-dots?
                                 16
                                 48))
                       string/join
                       (str " "))]
    dots-text))

(defn dots-comp
  "Returns the dots adding their styles"
  [few-of-dots?]
  [rn/view {:style {:align-items :center
                    :bottom      5}}
   [rn/text line-dots-props (dots few-of-dots?)]])

(defn lowest-price-value-comp
  "Component responsible for the top and bottom values"
  [top-value top-value-bg-color top-value-text-color]
  [rn/view (border-alignment top-value-bg-color)
   [rn/text {:style {:color      top-value-text-color
                     :text-align :center}} top-value]])

(defn lowest-price-styles [margin-top]
  {:style {:flex-direction  :column
           :width           "100%"
           :overflow        :hidden
           :margin-top      (int margin-top)
           :justify-content :center}})

(defn lowest-price
  "Shows the lowest price component"
  [{:keys [top-value
           bottom-value
           margin-top
           top-value-text-color
           top-value-bg-color
           bottom-value-bg-color
           bottom-value-text-color]}]
  [rn/view (lowest-price-styles margin-top)
   [rn/view centrify
    [dots-comp true]
    [lowest-price-value-comp top-value top-value-bg-color top-value-text-color]
    [dots-comp false]]
   [rn/view centrify
    [dots-comp false]
    [lowest-price-value-comp bottom-value bottom-value-bg-color bottom-value-text-color]
    [dots-comp true]]])
