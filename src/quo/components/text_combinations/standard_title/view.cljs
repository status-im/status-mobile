(ns quo.components.text-combinations.standard-title.view
  (:require [clojure.string :as string]
            [quo.components.buttons.button.view :as button]
            [quo.components.counter.fraction-counter.view :as fraction-counter]
            [quo.components.markdown.text :as text]
            [quo.components.tags.tag :as tag]
            [quo.components.text-combinations.standard-title.style :as style]
            [quo.context]
            [react-native.core :as rn]
            [utils.number]))

(defn- right-counter
  [{:keys [blur? counter-left counter-right counter-suffix show-counter-warning?]}]
  [rn/view {:style style/right-counter}
   [fraction-counter/view
    {:blur?                 blur?
     :show-counter-warning? show-counter-warning?
     :left-value            counter-left
     :right-value           counter-right
     :suffix                counter-suffix}]])

(defn- right-action
  [{:keys [customization-color on-press icon]
    :or   {icon :i/placeholder}}]
  [button/button
   {:accessibility-label :standard-title-action
    :size                32
    :icon-only?          true
    :customization-color customization-color
    :on-press            on-press}
   icon])

(defn- right-tag
  [{:keys [blur? on-press icon label]
    :or   {icon :i/placeholder}}]
  (let [theme     (quo.context/use-theme)
        labelled? (not (string/blank? label))]
    [tag/tag
     {:accessibility-label :standard-title-tag
      :size                32
      :type                :icon
      :resource            icon
      :on-press            on-press
      :labelled?           labelled?
      :label               (when labelled? label)
      :blurred?            blur?
      :icon-color          (style/right-tag-icon-color blur? theme)}]))

(defn view
  [{:keys [title right accessibility-label container-style] :as props}]
  [rn/view {:style (merge style/container container-style)}
   [text/text
    {:size                :heading-1
     :weight              :semi-bold
     :style               style/text
     :accessibility-label accessibility-label}
    title]
   (case right
     :counter [right-counter props]
     :action  [right-action props]
     :tag     [right-tag props]
     right)])
