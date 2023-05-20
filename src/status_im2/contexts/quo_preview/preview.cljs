(ns status-im2.contexts.quo-preview.preview
  (:require [clojure.string :as string]
            [quo2.foundations.colors :as colors]
            [react-native.blur :as blur]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [quo2.theme :as theme])
  (:require-macros status-im2.contexts.quo-preview.preview))

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
   :background-color   colors/neutral-20
   :border-width       1
   :border-color       colors/neutral-100})

(defn select-option-style
  [selected]
  (merge (select-style)
         {:margin-vertical 8
          :justify-content :center}
         (if selected
           {:background-color colors/primary-50-opa-30}
           {:background-color (colors/theme-colors colors/neutral-20 colors/white)})))

(def label-style
  {:flex          0.4
   :padding-right 8})

(defn label-view
  [_ label]
  [rn/view {:style label-style}
   [rn/text
    (when-let [label-color (colors/theme-colors colors/neutral-100 colors/white)]
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
   :margin-vertical    100
   :background-color   (colors/theme-colors colors/neutral-20 colors/white)})

(defn customizer-boolean
  [{:keys [label key state]}]
  (let [state* (reagent/cursor state [key])]
    [rn/view {:style container}
     [label-view state label]
     [rn/view
      {:style {:flex-direction   :row
               :flex             0.6
               :border-radius    4
               :background-color (colors/theme-colors colors/neutral-20 colors/white)
               :border-width     1
               :border-color     (colors/theme-colors colors/neutral-100 colors/white)}}
      [rn/touchable-opacity
       {:style    (merge (touchable-style) {:background-color (when @state* colors/primary-50-opa-30)})
        :on-press #(reset! state* true)}
       [rn/text
        "True"]]
      [rn/view
       {:width            1
        :margin-vertical  4
        :background-color (colors/theme-colors colors/neutral-20 colors/white)}]
      [rn/touchable-opacity
       {:style    (merge (touchable-style)
                         {:background-color (when (not @state*) colors/primary-50-opa-30)})
        :on-press #(reset! state* false)}
       [rn/text {}
        "False"]]]]))

(defn customizer-text
  [{:keys [label key state limit suffix]}]
  (let [state* (reagent/cursor state [key])]
    [rn/view {:style container}
     [label-view state label]
     [rn/view {:style {:flex 0.6}}
      [rn/text-input
       (merge
        {:value               @state*
         :show-cancel         false
         :style               {:border-radius 4
                               :border-width  1
                               :color         (colors/theme-colors colors/neutral-100 colors/white)
                               :border-color  (colors/theme-colors colors/neutral-100 colors/white)}
         :keyboard-appearance (theme/theme-value :light :dark)
         :on-change-text      #(do
                                 (reset! state* (if (and suffix (> (count %) (count @state*)))
                                                  (str (string/replace % suffix "") suffix)
                                                  %))
                                 (reagent/flush))}
        (when limit
          {:max-length limit}))]]]))

(defn value-for-key
  [id v]
  (:value (first (filter #(= (:key %) id) v))))

(defn customizer-select
  []
  (let [open (reagent/atom nil)]
    (fn [{:keys [label key state options]}]
      (let [state*   (reagent/cursor state [key])
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
               (for [{:keys [key value]} options]
                 ^{:key key}
                 [rn/touchable-opacity
                  {:style    (select-option-style (= @state* key))
                   :on-press #(do
                                (reset! open false)
                                (reset! state* key))}
                  [rn/text {:color (if (= @state* key) :link :secondary)}
                   value]]))]
             [rn/view
              {:flex-direction   :row
               :padding-top      20
               :margin-top       10
               :border-top-width 1
               :border-top-color (colors/theme-colors colors/neutral-100 colors/white)}
              [rn/touchable-opacity
               {:style    (select-option-style false)
                :on-press #(do
                             (reset! state* nil)
                             (reset! open false))}
               [rn/text "Clear"]]
              [rn/view {:width 16}]
              [rn/touchable-opacity
               {:style    (select-option-style false)
                :on-press #(reset! open false)}
               [rn/text "Close"]]]]]]

          [rn/touchable-opacity
           {:style    (select-style)
            :on-press #(reset! open true)}
           (if selected
             [rn/text {:color :link} selected]
             [rn/text "Select option"])
           [rn/view
            {:position        :absolute
             :right           16
             :top             0
             :bottom          0
             :justify-content :center}
            [rn/text "↓"]]]]]))))

(defn customizer
  [state descriptors]
  [rn/view
   {:style              {:flex 1}
    :padding-horizontal 16}
   (doall
    (for [{:keys [key type]
           :as   desc}
          descriptors
          :let [descriptor (merge desc
                                  {:state state})]]
      ^{:key key}
      [:<>
       (case type
         :boolean [customizer-boolean descriptor]
         :text    [customizer-text descriptor]
         :select  [customizer-select descriptor])]))])

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

(defn blur-view
  [{:keys [show-blur-background? image height style]} children]
  [rn/view
   {:style {:flex             1
            :padding-vertical 16}}
   (when show-blur-background?
     [rn/view
      {:style {:height        (or height 100)
               :border-radius 16
               :overflow      :hidden}}
      [rn/image
       {:source (or image (resources/get-mock-image :community-cover))
        :style  {:height "100%"
                 :width  "100%"}}]
      [blur/webview-blur
       {:style         {:position :absolute
                        :top      0
                        :bottom   0
                        :left     0
                        :right    0}
        :blur-amount   10
        :overlay-color (colors/theme-colors
                        colors/white-opa-70
                        colors/neutral-80-opa-80)}]
      ;[blur/view
      ; (merge {:style         {:position :absolute
      ;                         :top      0
      ;                         :bottom   0
      ;                         :left     0
      ;                         :right    0}
      ;         :blur-amount   10
      ;         :overlay-color (colors/theme-colors
      ;                         colors/white-opa-70
      ;                         colors/neutral-80-opa-80)}
      ;        blur-view-props)]
     ])
   [rn/view
    {:style (merge {:position           :absolute
                    :top                32
                    :padding-horizontal 16}
                   style)}
    children]])
