(ns quo2.components.drawers.drawer-buttons.view
  (:require [react-native.core :as rn]
            [quo2.components.icon :as icon]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.components.drawers.drawer-buttons.style :as style]))

(defn render-bottom
  [children]
  [rn/view
   {:flex-direction  :row
    :justify-content :space-between}
   [children]
   [rn/view
    {:style {:border-radius   40
             :border-width    1
             :margin-left     24
             :height          28
             :width           28
             :justify-content :center
             :align-items     :center
             :border-color    (colors/alpha colors/white 0.05)}}
    [icon/icon :arrow-right
     {:color colors/white

      :size  20}]]])

(defn render-children-bottom
  [children]
  (if (or (string? children) (keyword children))
    [render-bottom
     (fn []
       [text/text
        {:size   :paragraph-2
         :style  {:flex  1
                  :color (colors/alpha colors/white 0.7)}
         :weight :semi-bold}
        children])]
    [render-bottom children]))

(defn render-children-top
  [children]
  (if (or (string? children) (keyword children))
    [text/text
     {:size   :paragraph-2
      :style  {:color (colors/alpha colors/white 0.7)}
      :weight :semi-bold}
     children]
    [children]))

(defn card
  [{:keys [on-press style heading children gap is-top?]}]
  [rn/touchable-highlight
   {:on-press       on-press
    :border-radius  20
    :style          style
    :underlay-color (:background-color style)}
   [rn/view {}
    [text/text
     {:size   :heading-1
      :style  {:color         colors/white
               :margin-bottom gap}

      :weight :semi-bold}
     heading]
    (if is-top?
      [render-children-top children]
      [render-children-bottom children])]])

(defn view
  "[view opts]
   opts
   {:container-style  style-object
    :top-card         { :on-press event
                        :heading  string 
                        :children string or render-fn}
    :bottom-card      { :on-press event
                        :heading  string 
                        :children string or render-fn}}"
  [{:keys [container-style top-card bottom-card]}]
  [rn/view
   {:style (merge container-style style/outer-container)}
   [card
    (merge {:gap     4
            :is-top? true
            :style   style/top-card}
           top-card)]
   [card
    (merge {:style style/bottom-card
            :gap   20}
           bottom-card)]])
