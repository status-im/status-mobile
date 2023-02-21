(ns quo2.components.dividers.divider-label
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as markdown.text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(def chevron-icon-container-width 20)

(def chevron-icon-container-height 20)

(defn divider-label
  "label -> string
   chevron-position -> :left, :right
   counter-value -> number
   increase-padding-top? -> boolean
   blur? -> boolean"
  [{:keys [label chevron-position counter-value increase-padding-top? blur? container-style]}]
  (let [dark?                       (colors/dark?)
        border-and-counter-bg-color (if dark?
                                      (if blur? colors/white-opa-5 colors/neutral-70)
                                      colors/neutral-10)
        padding-top                 (if increase-padding-top? 16 8)
        text-and-icon-color         (if dark? colors/neutral-40 colors/neutral-50)
        counter-text-color          (if dark? colors/white colors/neutral-100)]
    [rn/view
     {:accessible          true
      :accessibility-label :divider-label
      :style               (merge {:border-top-width   1
                                   :border-top-color   border-and-counter-bg-color
                                   :padding-top        padding-top
                                   :padding-horizontal 16
                                   :align-items        :center
                                   :flex-direction     :row}
                                  container-style)}
     (when (= chevron-position :left)
       [rn/view
        {:test-ID :divider-label-icon-left
         :style   {:margin-right 4}}
        [icons/icon
         :main-icons/chevron-down
         {:color  text-and-icon-color
          :width  chevron-icon-container-width
          :height chevron-icon-container-height}]])
     [markdown.text/text
      {:size   :paragraph-2
       :weight :medium
       :style  {:color text-and-icon-color
                :flex  1}}
      label]
     (when (= chevron-position :right)
       [rn/view {:test-ID :divider-label-icon-right}
        [icons/icon
         :main-icons/chevron-down
         {:color text-and-icon-color
          :size  chevron-icon-container-width}]])
     (when (pos? counter-value)
       [rn/view
        {:style {:border-radius    6
                 :height           16
                 :width            (case (count counter-value)
                                     1 16
                                     2 20
                                     28)
                 :background-color border-and-counter-bg-color
                 :align-items      :center
                 :justify-content  :center}}
        [markdown.text/text
         {:size   :label
          :weight :medium
          :style  {:color counter-text-color}}
         counter-value]])]))
