(ns quo2.components.dividers.divider-label
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as markdown.text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]))

(def chevron-icon-container-width 20)

(def chevron-icon-container-height 20)

(defn themed-view
  "label -> string
   chevron-position -> :left, :right
   chevron-icon -> keyword
   on-press -> function
   padding-bottom -> number
   counter-value -> number
   increase-padding-top? -> boolean
   blur? -> boolean
   theme -> theme value passed from with-theme HOC"
  [{:keys [label
           chevron-position
           chevron-icon
           counter-value
           increase-padding-top?
           padding-bottom
           blur?
           container-style
           on-press
           theme]}]
  (let [dark?                       (= :dark theme)
        border-and-counter-bg-color (if dark?
                                      (if blur? colors/white-opa-5 colors/neutral-70)
                                      colors/neutral-10)
        padding-top                 (if increase-padding-top? 16 8)
        text-and-icon-color         (if dark? colors/neutral-40 colors/neutral-50)
        counter-text-color          (if dark? colors/white colors/neutral-100)]
    [rn/touchable-without-feedback {:on-press on-press}
     [rn/view
      {:accessible          true
       :accessibility-label :divider-label
       :style               (merge {:border-top-width 1
                                    :border-top-color border-and-counter-bg-color
                                    :padding-top      padding-top
                                    :padding-bottom   padding-bottom
                                    :padding-left     16
                                    :padding-right    16
                                    :align-items      :center
                                    :flex-direction   :row}
                                   container-style)}
      (when (= chevron-position :left)
        [rn/view
         {:test-ID :divider-label-icon-left
          :style   {:margin-right 4}}
         [icons/icon
          (or chevron-icon :i/chevron-down)
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
          (or chevron-icon :i/chevron-down)
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
          counter-value]])]]))

(def divider-label (theme/with-theme themed-view))
