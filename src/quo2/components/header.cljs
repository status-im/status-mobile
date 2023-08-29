(ns quo2.components.header
  (:require [oops.core :refer [oget]]
            [quo2.components.buttons.button.view :as button]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.reanimated :as reanimated]
            [reagent.core :as reagent]))

(def header-height 56)

(defn header-wrapper-style
  [{:keys [height border-bottom background]}]
  (merge
   {:background-color (colors/theme-colors
                       colors/neutral-5
                       colors/neutral-95)
    :height           height}
   (when background
     {:background-color background})
   (when border-bottom
     {:border-bottom-width 1
      :border-bottom-color (colors/theme-colors
                            colors/neutral-5
                            colors/neutral-95)})))

(def absolute-fill
  {:position :absolute
   :top      0
   :bottom   0
   :left     0
   :right    0})

(def content
  {:flex            1
   :flex-direction  :row
   :align-items     :center
   :justify-content :center})

(def left-style
  {:position        :absolute
   :left            0
   :top             0
   :bottom          0
   :justify-content :center
   :align-items     :flex-start})

(def right-style
  {:position        :absolute
   :right           0
   :top             0
   :bottom          0
   :justify-content :center
   :align-items     :flex-end})

(defn title-style
  [{:keys [left right]} title-align]
  (merge
   absolute-fill
   (case title-align
     :left {:left  (:width left)
            :right (:width right)}
     {:align-items     :center
      :justify-content :center
      :left            (max (:width left) (:width right))
      :right           (max (:width left) (:width right))})))

(def header-actions-style
  {:flex               1
   :flex-direction     :row
   :align-items        :center
   :justify-content    :center
   :padding-horizontal 4})

(def header-action-placeholder
  {:width 16})

(def element
  {:align-items     :center
   :justify-content :center
   :flex            1})

(defn header-action
  [{:keys [icon label on-press disabled accessibility-label]}]
  [button/button
   {:on-press            on-press
    :disabled?           disabled
    :icon-only?          (boolean icon)
    :accessibility-label accessibility-label}
   (cond
     icon  icon
     label label)])

(defn header-actions
  [{:keys [accessories component]}]
  [rn/view {:style element}
   (cond
     (seq accessories)
     (into [rn/view {:style header-actions-style}]
           (map header-action accessories))

     component component

     :else
     [rn/view {:style header-action-placeholder}])])

(defn header-title
  [{:keys [title subtitle component title-align]}]
  [:<>
   (cond
     component component

     (and title subtitle)
     [:<>
      [text/text
       {:weight          :medium
        :number-of-lines 1}
       title]
      [text/text
       {:weight          :regular
        :color           :secondary
        :number-of-lines 1}
       subtitle]]

     title [text/text
            {:weight          :bold
             :number-of-lines 0
             :align           title-align
             :size            :large}
            title])])

(defn header
  [{:keys [left-width right-width]}]
  (let [layout        (reagent/atom {:left  {:width  (or left-width 8)
                                             :height header-height}
                                     :right {:width  (or right-width 8)
                                             :height header-height}
                                     :title {:width  0
                                             :height header-height}})
        handle-layout (fn [el get-layout]
                        (fn [evt]
                          (let [width  (oget evt "nativeEvent" "layout" "width")
                                height (oget evt "nativeEvent" "layout" "height")]
                            (when get-layout
                              (get-layout el
                                          {:width  width
                                           :height height}))
                            (swap! layout assoc
                              el
                              {:width  width
                               :height height}))))]
    (fn
      [{:keys [left-accessories left-component border-bottom
               right-accessories right-component insets get-layout
               title subtitle title-component style title-align
               background]
        :or   {title-align   :center
               border-bottom false}}]
      (let [status-bar-height (get insets :top 0)
            height            (+ header-height status-bar-height)]
        [reanimated/view
         {:style (header-wrapper-style {:height        height
                                        :background    background
                                        :border-bottom border-bottom})}
         [rn/view
          {:pointer-events :box-none
           :height         status-bar-height}]
         [rn/view
          {:style          (merge {:height header-height}
                                  style)
           :pointer-events :box-none}
          [rn/view
           {:style          absolute-fill
            :pointer-events :box-none}
           [rn/view
            {:style          content
             :pointer-events :box-none}
            [rn/view
             {:style          left-style
              :on-layout      (handle-layout :left get-layout)
              :pointer-events :box-none}
             [header-actions
              {:accessories left-accessories
               :component   left-component}]]

            [rn/view
             {:style          (title-style @layout title-align)
              :on-layout      (handle-layout :title get-layout)
              :pointer-events :box-none}
             [header-title
              {:title       title
               :subtitle    subtitle
               :title-align title-align
               :component   title-component}]]

            [rn/view
             {:style          right-style
              :on-layout      (handle-layout :right get-layout)
              :pointer-events :box-none}
             [header-actions
              {:accessories right-accessories
               :component   right-component}]]]]]]))))
