(ns quo2.components.links.url-preview.view
  (:require
    [quo2.components.icon :as icon]
    [quo2.components.links.url-preview.style :as style]
    [quo2.components.markdown.text :as text]
    [quo2.foundations.colors :as colors]
    [react-native.core :as rn]))

(defn- logo-component
  [{:keys [logo]}]
  [rn/image
   {:accessibility-label :logo
    :source              logo
    :style               style/logo}])

(defn- content
  [{:keys [title body]}]
  [rn/view {:style style/content-container}
   [text/text
    {:accessibility-label :title
     :size                :paragraph-2
     :weight              :semi-bold
     :number-of-lines     1
     :style               (style/title)}
    title]
   [text/text
    {:accessibility-label :body
     :size                :paragraph-2
     :weight              :medium
     :number-of-lines     1
     :style               (style/body)}
    body]])

(defn- clear-button
  [{:keys [on-press]}]
  [rn/touchable-opacity
   {:on-press            on-press
    :style               style/clear-button-container
    :hit-slop            {:top 3 :right 3 :bottom 3 :left 3}
    :accessibility-label :button-clear-preview}
   [icon/icon :i/clear
    {:size             20
     :background-color (colors/theme-colors colors/neutral-50 colors/neutral-60)
     :foreground-color colors/white}]])

(defn view
  [{:keys [title body logo on-clear loading? loading-message container-style]}]
  (if loading?
    [rn/view
     {:accessibility-label :url-preview-loading
      :style               (merge (style/loading-container) container-style)}
     [rn/text
      {:size            :paragraph-2
       :weight          :medium
       :number-of-lines 1
       :style           (style/loading-message)}
      loading-message]]
    [rn/view
     {:accessibility-label :url-preview
      :style               (merge (style/container) container-style)}
     [logo-component {:logo logo}]
     [content {:title title :body body}]
     [clear-button {:on-press on-clear}]]))
