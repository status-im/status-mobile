(ns quo2.components.tags.token-tag
  (:require [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]))

(def themes
  {:light {:background-color colors/neutral-20}
   :dark  {:background-color colors/neutral-80}})

(defn get-value-from-size
  [size big-option small-option]
  (if (= size :big) big-option small-option))

(def icon-container-styles
  {:display         :flex
   :align-items     :center
   :justify-content :center
   :position        :absolute
   :border-radius   20
   :margin-left     2})

(defn tag-container
  [size]
  {:height         (get-value-from-size size 32 24)
   :align-items    :center
   :flex-direction :row
   :border-radius  20})

(defn tag
  "[tag opts \"label\"]]
   opts
   {
    :size :small/:big
    :token-img-src :token-img-src
    :token-img-style {}
    :border-color :color
    :overlay child-elements
    }"
  [_ _]
  (fn [{:keys [size img-src img-style border-color overlay]
        :or   {size :small}}
       label]
    [rn/view
     {:style (when border-color
               {:border-color  border-color
                :border-radius 20
                :border-width  1})}
     [rn/view
      {:style (merge (tag-container size) (get themes (theme/get-theme)))}
      [rn/image
       {:src   img-src
        :style (merge
                {:height       (get-value-from-size size 28 20)
                 :width        (get-value-from-size size 28 20)
                 :margin-left  2
                 :margin-right (get-value-from-size size 8 6)}
                img-style)}]
      [text/text
       {:weight :medium
        :number-of-lines 1
        :style
        {:margin-right (get-value-from-size size 12 11)}
        :size (get-value-from-size size :paragraph-2 :label)} label]
      overlay]]))

(defn loading-icon
  []
  [rn/view
   {:style {:align-items     :center
            :justify-content :center
            :position        :absolute
            :right           -8
            :top             -8}}
   [icons/icon :t/checktoken
    {:no-color true
     :size     20}]])

(defn icon
  [size border-color sufficient?]
  [rn/view
   {:style (merge
            icon-container-styles
            {:background-color border-color
             :border-color     (if (= (theme/get-theme) :dark) colors/neutral-100 colors/white)
             :border-width     1
             :right            (get-value-from-size size -3.75 -5.75)
             :bottom           (get-value-from-size size (- 32 7.75 4) (- 24 7.75 2))})}
   [icons/icon (if sufficient? :i/hold :i/add)
    {:no-color true
     :size     12}]])

(defn token-tag
  "[token-tag opts]
   opts
   {
    :symbol string
    :value string
    :size :small/:big
    :token-img-src :token-img-src
    :border-color :color
    :sufficient? true/false
    :purchasable? true/false
    :loading? true/false
    }"
  [_ _]
  (fn [{:keys [symbol value size img-src border-color purchasable? sufficient? loading?]
        :or
        {size :small}}]
    (let [sufficient?  (when-not loading? sufficient?)
          border-color (if sufficient? colors/success-50 border-color)]
      [tag
       {:size size
        :img-src img-src
        :border-color (if sufficient? colors/success-50 border-color)
        :overlay
        (if loading?
          [loading-icon]
          (when (or purchasable? sufficient?)
            [icon size border-color sufficient?]))}
       (str value " " symbol)])))
