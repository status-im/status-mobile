(ns quo2.components.tags.token-tag
  (:require [quo2.foundations.colors :as colors]
            [quo.react-native :as rn]
            [quo.theme :as theme]
            [status-im.ui.components.icons.icons :as icons]
            [quo2.components.tags.tag :as tag]))

(defn get-value-from-size [size big-option small-option]
  (if  (= size :big) big-option small-option))

(def icon-container-styles
  {:display :flex
   :align-items :center
   :justify-content :center
   :position :absolute
   :border-radius 20
   :margin-left 2})

(defn token-tag
  "[token-tag opts]
   opts
   {
    :token string
    :value string
    :size :small/:big
    :token-img-src :token-img-src
    :border-color :color
    :is-required true/false
    :is-purchasable true/false
    }"
  [_ _]
  (fn [{:keys [token value size token-img-src border-color is-required is-purchasable]
        :or
        {size :small border-color (colors/custom-color-by-theme :purple 50 60)}}]

    [tag/tag {:size size
              :token-img-src token-img-src
              :border-color (when is-required border-color)
              :overlay
              (when (or is-required is-purchasable)
                [rn/view
                 {:style (merge icon-container-styles
                                {:width 15.5
                                 :height 15.5
                                 :background-color border-color
                                 :border-color (if (=  (theme/get-theme) :dark) colors/neutral-100 colors/white)
                                 :border-width 1
                                 :right  (get-value-from-size size -3.75 -5.75)
                                 :bottom (get-value-from-size size (- 32 7.75 4) (- 24 7.75 2)) ; (- height (icon-height/2) spacing)
                                 })}
                 [icons/icon (if is-required :main-icons2/required-checkmark12 :main-icons2/purchasable12)
                  {:no-color true
                   :width 13.5
                   :height 13.5}]])}
     (str value " " token)]))
