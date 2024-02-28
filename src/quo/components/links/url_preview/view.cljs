(ns quo.components.links.url-preview.view
  (:require
    [clojure.string :as string]
    [quo.components.icon :as icon]
    [quo.components.links.url-preview.style :as style]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [react-native.core :as rn]
    [react-native.svg :as svg]
    [status-im.constants :as constants]))

(defn- logo-comp
  [{:keys [logo]}]
  (if (string/starts-with? logo constants/base64-svg-prefix)
    [svg/svg-xml
     (merge style/logo
            {:xml (-> logo
                      (string/replace constants/base64-svg-prefix "")
                      js/atob)})]
    [rn/image
     {:accessibility-label :logo
      :source              (if (string? logo)
                             {:uri logo}
                             logo)
      :style               style/logo
      :resize-mode         :cover}]))

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
    {:size  20
     :color (colors/theme-colors colors/neutral-50 colors/neutral-60)}]])

(defn view
  [{:keys [title body logo on-clear loading? loading-message container-style]}]
  (if loading?
    [rn/view
     {:accessibility-label :url-preview-loading
      :style               (merge (style/loading-container) container-style)}
     [icon/icon :i/loading
      {:size            12
       :color           (colors/theme-colors colors/neutral-50 colors/neutral-40)
       :container-style {:margin-right 8}}]
     [rn/text
      {:size            :paragraph-2
       :weight          :medium
       :number-of-lines 1
       :style           (style/loading-message)}
      loading-message]]
    [rn/view
     {:accessibility-label :url-preview
      :style               (merge (style/container) container-style)}
     (when logo
       [logo-comp
        {:logo logo}])
     [content {:title title :body body}]
     [clear-button {:on-press on-clear}]]))
