(ns quo2.components.links.link-preview.view
  (:require [quo2.components.buttons.button.view :as button]
            [quo2.components.links.link-preview.style :as style]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]))

(defn- button-disabled
  [disabled-text on-enable]
  [button/button
   {:icon-left           :i/reveal
    :size                32
    :type                :grey
    :on-press            on-enable
    :accessibility-label :button-enable-preview}
   disabled-text])

(defn- description-comp
  [description]
  [text/text
   {:size                :paragraph-2
    :number-of-lines     3
    :accessibility-label :description}
   description])

(defn- link-comp
  [link]
  [text/text
   {:size                :paragraph-2
    :weight              :medium
    :style               (style/link)
    :accessibility-label :link}
   link])

(defn- title-comp
  [title]
  [text/text
   {:size                :paragraph-1
    :number-of-lines     1
    :weight              :semi-bold
    :style               style/title
    :accessibility-label :title}
   title])

(defn- thumbnail-comp
  [thumbnail size]
  [rn/image
   {:style               (style/thumbnail size)
    :source              (if (string? thumbnail)
                           {:uri thumbnail}
                           thumbnail)
    :accessibility-label :thumbnail}])

(defn- logo-comp
  [logo]
  [rn/image
   {:accessibility-label :logo
    :source              (if (string? logo)
                           {:uri logo}
                           logo)
    :style               style/logo}])

(defn view
  [{:keys [title logo description link thumbnail
           enabled? on-enable disabled-text
           container-style thumbnail-size]
    :or   {enabled? true}}]
  [rn/view
   {:style               (merge (style/container enabled?) container-style)
    :accessibility-label :link-preview}
   (if enabled?
     [:<>
      [rn/view {:style style/header-container}
       (when logo
         [logo-comp logo])
       [title-comp title]]
      (when description
        [description-comp description])
      [link-comp link]
      (when thumbnail
        [thumbnail-comp thumbnail thumbnail-size])]
     [button-disabled disabled-text on-enable])])
