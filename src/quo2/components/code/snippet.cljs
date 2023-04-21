(ns quo2.components.code.snippet
  (:require [cljs-bean.core :as bean]
            [clojure.string :as string]
            [quo2.components.buttons.button :as button]
            [quo2.components.code.code.style :as style]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]
            [react-native.linear-gradient :as linear-gradient]
            [react-native.syntax-highlighter :as highlighter]
            [reagent.core :as reagent]))

(defn- render-nodes
  [nodes]
  (map (fn [{:keys [children value last-line?] :as node}]
         (if children
           (into [text/text
                  (cond-> {:weight :code
                           :size   :paragraph-2
                           :style  (style/text-style (get-in node [:properties :className]))}
                    last-line? (assoc :number-of-lines 1))]
                 (render-nodes children))
           ;; Remove newlines as we already render each line separately.
           (string/trim-newline value)))
       nodes))

(defn- line
  [{:keys [line-number line-number-width]} children]
  [rn/view {:style style/line}
   [rn/view {:style (style/line-number line-number-width)}
    [text/text
     {:style  (style/text-style ["line-number"])
      :weight :code
      :size   :paragraph-2}
     line-number]]
   children])

(defn- code-block
  [{:keys [rows line-number-width]}]
  [rn/view
   (->> rows
        (render-nodes)
        (map-indexed (fn [idx row-content]
                       [line
                        {:line-number       (inc idx)
                         :line-number-width line-number-width}
                        row-content]))
        (into [:<>]))])

(defn- mask-view
  [{:keys [apply-mask?]} child]
  (if apply-mask?
    [:<>
     [rn/view {:style style/gradient-container}
      [linear-gradient/linear-gradient
       {:style  style/gradient
        :colors [:transparent (style/gradient-color)]}
       [rn/view {:style style/gradient}]]]
     child]
    child))

(defn- calc-line-number-width
  [font-scale rows-to-show]
  (let [max-line-digits (-> rows-to-show str count)]
    (if (= 1 max-line-digits)
      18 ;; ~ 9 is char width, 18 is width used in Figma.
      (* 9 max-line-digits font-scale))))

(defn- f-native-renderer
  [{:keys [rows max-lines on-copy-press]
    :or   {max-lines ##Inf}}]
  (let [font-scale          (:font-scale (rn/get-window))
        total-rows          (count rows)
        number-rows-to-show (min (count rows) max-lines)
        line-number-width   (calc-line-number-width font-scale number-rows-to-show)
        truncated?          (< number-rows-to-show total-rows)
        rows-to-show-coll   (if truncated?
                              (as-> rows $
                                (update $ number-rows-to-show assoc :last-line? true)
                                (take (inc number-rows-to-show) $))
                              rows)]
    [rn/view {:style (style/container)}
     [rn/view {:style (style/line-number-container line-number-width)}]
     [rn/view {:style (style/divider line-number-width)}]
     [mask-view {:apply-mask? truncated?}
      [code-block
       {:rows              rows-to-show-coll
        :line-number-width line-number-width}]]
     [rn/view {:style style/copy-button}
      [button/button
       {:icon                      true
        :type                      :grey
        :size                      24
        :on-press                  on-copy-press
        :override-background-color (style/button-background-color)}
       :main-icons/copy]]]))

(defn snippet
  [{:keys [language max-lines on-copy-press]} children]
  [highlighter/highlighter
   {:language          language
    :renderer          (fn [^js/Object props]
                         (reagent/as-element
                          [:f> f-native-renderer
                           {:rows          (-> props .-rows bean/->clj)
                            :on-copy-press #(when on-copy-press (on-copy-press children))
                            :max-lines     max-lines}]))
    :show-line-numbers false
    :style             {}
    :custom-style      {:background-color nil}}
   children])
