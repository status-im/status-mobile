(ns quo.previews.preview
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [quo.react-native :as rn]
            [reagent.core :as reagent])
  (:require-macros quo.previews.preview))

(def container
  {:flex-direction   :row
   :padding-vertical 8
   :flex             1
   :align-items      :center})

(defn touchable-style
  []
  {:flex               1
   :align-items        :center
   :justify-content    :center
   :padding-horizontal 16
   :height             44})

(defn select-style
  []
  {:flex               1
   :flex-direction     :row
   :align-items        :center
   :padding-horizontal 16
   :height             44
   :border-radius      4
   :background-color   (:ui-01 @colors/theme)
   :border-width       1
   :border-color       (:ui-02 @colors/theme)})

(defn select-option-style
  [selected]
  (merge (select-style)
         {:margin-vertical 8
          :justify-content :center}
         (if selected
           {:background-color (:interactive-02 @colors/theme)}
           {:background-color (:ui-01 @colors/theme)})))

(def label-style
  {:flex          0.4
   :padding-right 8})

(defn label-view
  [state label]
  [rn/view {:style label-style}
   [quo/text
    (when-let [label-color (:preview-label-color @state)]
      {:style {:color label-color}})
    label]])

(defn modal-container
  []
  {:flex               1
   :justify-content    :center
   :padding-horizontal 24
   :background-color   "rgba(0,0,0,0.4)"})

(defn modal-view
  []
  {:padding-horizontal 16
   :padding-vertical   8
   :border-radius      8
   :flex-direction     :column
   :background-color   (:ui-background @colors/theme)})

(defn customizer-boolean
  [{:keys [label state] :as args}]
  (let [state* (reagent/cursor state [(:key args)])]
    [rn/view {:style container}
     [label-view state label]
     [rn/view
      {:style {:flex-direction   :row
               :flex             0.6
               :border-radius    4
               :background-color (:ui-01 @colors/theme)
               :border-width     1
               :border-color     (:ui-02 @colors/theme)}}
      [rn/touchable-opacity
       {:style    (touchable-style)
        :on-press #(reset! state* true)}
       [quo/text {:color (if @state* :link :secondary)}
        "True"]]
      [rn/view
       {:width            1
        :margin-vertical  4
        :background-color (:ui-02 @colors/theme)}]
      [rn/touchable-opacity
       {:style    (touchable-style)
        :on-press #(reset! state* false)}
       [quo/text {:color (if (not @state*) :link :secondary)}
        "False"]]]]))

(defn customizer-text
  [{:keys [label state] :as args}]
  (let [state* (reagent/cursor state [(:key args)])]
    [rn/view {:style container}
     [label-view state label]
     [rn/view {:style {:flex 0.6}}
      [quo/text-input
       {:value          @state*
        :show-cancel    false
        :style          {:border-radius 4
                         :border-width  1
                         :border-color  (:ui-02 @colors/theme)}
        :on-change-text #(do
                           (reset! state* %)
                           (reagent/flush))}]]]))

(defn value-for-key
  [id v]
  (:value (first (filter #(= (:key %) id) v))))

(defn customizer-select
  []
  (let [open (reagent/atom nil)]
    (fn [{:keys [label state options] :as args}]
      (let [state*   (reagent/cursor state [(:key args)])
            selected (value-for-key @state* options)]
        [rn/view {:style container}
         [label-view state label]
         [rn/view {:style {:flex 0.6}}
          [rn/modal
           {:visible              @open
            :on-request-close     #(reset! open false)
            :statusBarTranslucent true
            :transparent          true
            :animation            :slide}
           [rn/view {:style (modal-container)}
            [rn/view {:style (modal-view)}
             [rn/scroll-view
              (doall
               (for [{k :key v :value} options]
                 ^{:key k}
                 [rn/touchable-opacity
                  {:style    (select-option-style (= @state* k))
                   :on-press #(do
                                (reset! open false)
                                (reset! state* k))}
                  [quo/text {:color (if (= @state* k) :link :secondary)}
                   v]]))
              [rn/view {:flex-direction :row}
               [rn/touchable-opacity
                {:style    (select-option-style false)
                 :on-press #(do
                              (reset! state* nil)
                              (reset! open false))}
                [quo/text "Clear"]]
               [rn/view {:width 16}]
               [rn/touchable-opacity
                {:style    (select-option-style false)
                 :on-press #(reset! open false)}
                [quo/text "Close"]]]]]]]

          [rn/touchable-opacity
           {:style    (select-style)
            :on-press #(reset! open true)}
           (if selected
             [quo/text {:color :link} selected]
             [quo/text "Select option"])
           [rn/view
            {:position        :absolute
             :right           16
             :top             0
             :bottom          0
             :justify-content :center}
            [quo/text "↓"]]]]]))))

(defn customizer
  [state descriptors]
  [rn/view
   {:style              {:flex 1}
    :padding-horizontal 16}
   (doall
    (for [desc descriptors
          :let [descriptor (merge desc {:state state})]]
      ^{:key (:key desc)}
      [:<>
       (case (:type desc)
         :boolean [customizer-boolean descriptor]
         :text    [customizer-text descriptor]
         :select  [customizer-select descriptor]
         nil)]))])

(comment
  [{:label "Show error:"
    :key   :error
    :type  :boolean}
   {:label "Label:"
    :key   :label
    :type  :text}
   {:label   "Type:"
    :key     :type
    :type    :select
    :options [{:key :primary :value "Primary"}
              {:key :secondary :value "Secondary"}]}])
