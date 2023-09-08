(ns quo2.components.code.snippet-preview.view
  (:require [react-native.core :as rn]
            [react-native.syntax-highlighter :as highlighter]
            [reagent.core :as reagent]
            [cljs-bean.core :as bean]
            [quo2.components.code.snippet-preview.style :as style]
            [quo2.theme :as theme]
            [quo2.components.markdown.text :as text]
            [clojure.string :as string]))

(defn- render-nodes
  [nodes]
  (map (fn [{:keys [children value last-line?] :as node}]
         (println last-line? value)
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
   [rn/view
    {:style {:flex               1
             :padding-horizontal 8
             :padding-vertical   3}}
    children]])

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

(defn- f-native-renderer
  [{:keys [rows max-lines theme]
    :or   {max-lines ##Inf}}]
  (let [total-rows          (count rows)
        number-rows-to-show (min (count rows) max-lines)
        truncated?          (< number-rows-to-show total-rows)
        rows-to-show-coll   (if truncated?
                              (as-> rows $
                                (update $ number-rows-to-show assoc :last-line? true)
                                (take (inc number-rows-to-show) $))
                              rows)]
    [rn/view {:style (style/container theme)}
     [rn/view {:style (style/line-number-container theme)}]
     [code-block
      {:rows              rows-to-show-coll
       :line-number-width 20}]]))

(defn- view-internal
  [_]
  (fn [{:keys [language theme]} children]
    [highlighter/highlighter
     {:language          language
      :renderer          (fn [^js/Object props]
                           (reagent/as-element
                            [:f> f-native-renderer
                             {:rows      (-> props .-rows bean/->clj)
                              :max-lines 0
                              :theme     theme}]))
      :show-line-numbers false
      :style             {}
      :custom-style      {:background-color nil}}
     children]))

(def view (theme/with-theme view-internal))
