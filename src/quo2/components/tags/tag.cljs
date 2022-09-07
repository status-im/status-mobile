(ns quo2.components.tags.tag
  (:require [quo.react-native :as rn]
            [quo2.foundations.colors :as colors]
            [quo.theme :as theme]
            [quo2.components.markdown.text :as text]))

(def themes {:light {:background-color colors/neutral-20}
             :dark {:background-color colors/neutral-80}})

(defn get-value-from-size [size big-option small-option]
  (if  (= size :big) big-option small-option))

(defn tag-container [size]
  {:height (get-value-from-size size 32 24)
   :align-items :center
   :flex-direction :row
   :border-radius 20})

(defn tag  "[tag opts \"label\"]]
   opts
   {
    :size :small/:big
    :token-img-src :token-img-src
    :token-img-style {}
    :border-color :color
    :overlay child-elements
    }"
  [_ _]
  (fn [{:keys [size token-img-src token-img-style border-color overlay]
        :or {size :small}} label]
    [rn/view
     {:style (when border-color
               {:border-color border-color
                :border-radius 20
                :border-width 1})}
     [rn/view
      {:style   (merge (tag-container size) (get themes (theme/get-theme)))}
      [rn/image
       {:source token-img-src
        :style (merge
                {:height (get-value-from-size size 28 20)
                 :width (get-value-from-size size 28 20)
                 :margin-left 2
                 :margin-right  (get-value-from-size size 8 6)} token-img-style)}]
      [text/text
       {:weight :medium
        :number-of-lines 1
        :style
        {:margin-right (get-value-from-size size 12 11)}
        :size (get-value-from-size size :paragraph-2 :label)} label]
      overlay]]))