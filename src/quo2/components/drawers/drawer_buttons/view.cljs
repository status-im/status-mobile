(ns quo2.components.drawers.drawer-buttons.view
  (:require [react-native.core :as rn]
            [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.blur :as blur]
            [quo2.components.drawers.drawer-buttons.style :as style]))

(defn render-bottom
  [children]
  [rn/view {:style style/bottom-container}
   children
   [rn/view
    {:style style/bottom-icon}
    [icon/icon :arrow-right
     {:color colors/white
      :size  20}]]])

(defn label? [el] (or (string? el) (keyword? el)))

(defn render-children-bottom
  [children]
  (if (label? children)
    [render-bottom
     [text/text
      {:size   :paragraph-2
       :style  style/bottom-text
       :weight :semi-bold}
      children]]
    [render-bottom children]))

(defn render-children-top
  [children]
  (if (label? children)
    [text/text
     {:size   :paragraph-2
      :style  style/top-text
      :weight :semi-bold}
     children]
    children))

(defn card
  [{:keys [on-press style heading gap accessibility-label top?]} children]
  [rn/touchable-highlight
   {:accessibility-label accessibility-label
    :on-press            on-press
    :border-radius       20
    :style               style
    :underlay-color      (:background-color style)}
   [rn/view
    [text/text
     {:size   :heading-1
      :style  (style/heading-text gap)
      :weight :semi-bold}
     heading]
    (if top?
      [render-children-top children]
      [render-children-bottom children])]])

(defn view
  "[view opts]
   opts
   {:container-style  style-object
    :top-card         { :on-press event
                        :heading  string
                        :accessibility-label keyword}
    :bottom-card      { :on-press event
                        :heading  string
                        :accessibility-label keyword}}
    child-1           string, keyword or hiccup
    child-2           string, keyword or hiccup
   "
  [{:keys [container-style top-card bottom-card]} child-1 child-2]
  [rn/view
   {:style (merge container-style style/outer-container)}
   [blur/view
    {:blur-type   :dark
     :blur-amount 10
     :style       {:flex                    1
                   :border-top-left-radius  20
                   :border-top-right-radius 20}}]
   [rn/view
    {:style {:flex             1
             :background-color :transparent
             :position         :absolute
             :top              0
             :left             0
             :right            0
             :bottom           0}}
    [card
     (merge {:gap   4
             :top?  true
             :style style/top-card}
            top-card) child-1]
    [card
     (merge {:style style/bottom-card
             :gap   20}
            bottom-card) child-2]]])