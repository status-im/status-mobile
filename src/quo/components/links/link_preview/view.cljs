(ns quo.components.links.link-preview.view
  (:require
    ["react-native-blob-util" :default ReactNativeBlobUtil]
    [oops.core :as oops]
    [quo.components.buttons.button.view :as button]
    [quo.components.links.link-preview.style :as style]
    [quo.components.markdown.text :as text]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.svg :as svg]
    [reagent.core :as reagent]
    [taoensso.timbre :as log]))

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
  (let [xml-data   (reagent/atom nil)
        is-svg?    (reagent/atom nil)
        on-success (fn [xml-string]
                     (reset! xml-data xml-string))
        _get-svg   (-> (.config ReactNativeBlobUtil (clj->js {:trusty platform/ios?}))
                       (.fetch "GET" logo)
                       (.then #(do
                                 (reset! is-svg? (= "image/svg"
                                                    (oops/oget % ["respInfo" "headers" "Content-Type"])))
                                 (on-success (oops/oget % "data"))))
                       (.catch #(log/error "could not fetch favicon " logo)))]
    (fn []
      (if @is-svg?
        [svg/svg-xml (merge style/logo {:xml @xml-data})]
        [rn/image
         {:accessibility-label :logo
          :source              (if (string? logo)
                                 {:uri logo}
                                 logo)
          :style               style/logo}]))))

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
