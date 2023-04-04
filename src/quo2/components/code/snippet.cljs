(ns quo2.components.code.snippet
  (:require [cljs-bean.core :as bean]
            [clojure.string :as string]
            [oops.core :as oops]
            [quo2.components.buttons.button :as button]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [react-native.core :as rn]
            [react-native.linear-gradient :as linear-gradient]
            [react-native.masked-view :as masked-view]
            [react-native.syntax-highlighter :as highlighter]
            [reagent.core :as reagent]))

;; Example themes:
;; https://github.com/react-syntax-highlighter/react-syntax-highlighter/tree/master/src/styles/hljs
(def ^:private themes
  {:light {:hljs-comment {:color colors/neutral-40}
           :hljs-title   {:color (colors/custom-color :blue 50)}
           :hljs-keyword {:color (colors/custom-color :green 50)}
           :hljs-string  {:color (colors/custom-color :turquoise 50)}
           :hljs-literal {:color (colors/custom-color :turquoise 50)}
           :hljs-number  {:color (colors/custom-color :turquoise 50)}
           :line-number  {:color colors/neutral-40}}
   :dark  {:hljs-comment {:color colors/neutral-60}
           :hljs-title   {:color (colors/custom-color :blue 60)}
           :hljs-keyword {:color (colors/custom-color :green 60)}
           :hljs-string  {:color (colors/custom-color :turquoise 60)}
           :hljs-literal {:color (colors/custom-color :turquoise 60)}
           :hljs-number  {:color (colors/custom-color :turquoise 60)}
           :line-number  {:color colors/neutral-40}}})

(defn- text-style
  [class-names]
  (->> class-names
       (map keyword)
       (reduce #(merge %1 (get-in themes [(theme/get-theme) %2]))
               {:flex-shrink 1
                ;; Round to a nearest whole number to achieve consistent
                ;; spacing (also important for calculating `max-text-height`).
                ;; Line height seems to be inconsistent between text being
                ;; wrapped and text being rendered on a newline using Flexbox
                ;; layout.
                :line-height 18})))

(defn- render-nodes
  [nodes]
  (map (fn [{:keys [children value] :as node}]
         ;; Node can have :children or a :value.
         (if children
           (into [text/text
                  {:weight :code
                   :size   :paragraph-2
                   :style  (text-style (get-in node [:properties :className]))}]
                 (render-nodes children))
           ;; Remove newlines as we already render each line separately.
           (-> value string/trim-newline)))
       nodes))

(defn- code-block
  [{:keys [rows line-number-width]}]
  [into [:<>]
   (->> rows
        (render-nodes)
        ;; Line numbers
        (map-indexed (fn [idx row]
                       (conj [rn/view {:style {:flex-direction :row}}
                              [rn/view
                               {:style {:width        line-number-width
                                        ;; 8+12 margin
                                        :margin-right 20}}
                               [text/text
                                {:weight :code
                                 :size   :paragraph-2
                                 :style  (text-style ["line-number"])}
                                (inc idx)]]]
                             row))))])

(defn- native-renderer
  []
  (let [text-height (reagent/atom nil)]
    (fn [{:keys [rows max-lines on-copy-press]}]
      (let [background-color      (colors/theme-colors
                                   colors/white
                                   colors/neutral-80-opa-40)
            background-color-left (colors/theme-colors
                                   colors/neutral-5
                                   colors/neutral-80)
            border-color          (colors/theme-colors
                                   colors/neutral-20
                                   colors/neutral-80)
            rows                  (bean/->clj rows)
            font-scale            (:font-scale (rn/use-window-dimensions))
            max-rows              (or max-lines (count rows)) ;; Cut down on rows to process.
            max-line-digits       (-> rows count (min max-rows) str count)
            ;; ~ 9 is char width, 18 is width used in Figma.
            line-number-width     (* font-scale (max 18 (* 9 max-line-digits)))
            max-text-height       (some-> max-lines
                                          (* font-scale 18)) ;; 18 is font's line height.
            truncated?            (and max-text-height (< max-text-height @text-height))
            maybe-mask-wrapper    (if truncated?
                                    [masked-view/masked-view
                                     {:mask-element
                                      (reagent/as-element
                                       [linear-gradient/linear-gradient
                                        {:colors    ["black" "transparent"]
                                         :locations [0.75 1]
                                         :style     {:flex 1}}])}]
                                    [:<>])]

        [rn/view
         {:style {:overflow         :hidden
                  :padding          8
                  :background-color background-color
                  :border-color     border-color
                  :border-width     1
                  :border-radius    8
                  ;; Hide on intial render to avoid flicker when mask-wrapper is shown.
                  :opacity          (if @text-height 1 0)}}
         ;; Line number container
         [rn/view
          {:style {:position           :absolute
                   :bottom             0
                   :top                0
                   :left               0
                   :width              (+ line-number-width 8 8)
                   :background-color   background-color-left
                   :border-right-color border-color
                   :border-right-width 1}}]
         (conj maybe-mask-wrapper
               [rn/view {:max-height max-text-height}
                [rn/view
                 {:on-layout (fn [evt]
                               (let [height (oops/oget evt "nativeEvent.layout.height")]
                                 (reset! text-height height)))}
                 [code-block
                  {:rows              (take max-rows rows)
                   :line-number-width line-number-width}]]])

         ;; Copy button
         [rn/view
          {:style {:position :absolute
                   :bottom   8
                   :right    8}}
          [button/button
           {:icon     true
            :type     :grey
            :size     24
            :on-press on-copy-press}
           :main-icons/copy]]]))))

(defn- wrap-renderer-fn
  [f {:keys [max-lines on-copy-press]}]
  (fn [^js props]
    (reagent/as-element [:f> f
                         {:rows          (.-rows props)
                          :max-lines     max-lines
                          :on-copy-press on-copy-press}])))

(defn snippet
  [{:keys [language max-lines on-copy-press]} children]
  [highlighter/highlighter
   {:language          language
    :renderer          (wrap-renderer-fn
                        native-renderer
                        {:max-lines     max-lines
                         :on-copy-press #(when on-copy-press
                                           (on-copy-press children))})
    ;; Default props to adapt Highlighter for react-native.
    ;;:CodeTag           react-native/View
    ;;:PreTag            react-native/View
    :show-line-numbers false
    :style             #js {}
    :custom-style      #js {:backgroundColor nil}}
   #_children])
