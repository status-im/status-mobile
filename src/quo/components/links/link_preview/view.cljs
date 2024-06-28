(ns quo.components.links.link-preview.view
  (:require
    ["react-native-blob-util" :default ReactNativeBlobUtil]
    [oops.core :as oops]
    [quo.components.buttons.button.view :as button]
    [quo.components.links.link-preview.style :as style]
    [quo.components.markdown.text :as text]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.svg :as svg]
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
  [link theme]
  [text/text
   {:size                :paragraph-2
    :weight              :medium
    :style               (style/link theme)
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

(defn- get-image-data
  [logo set-is-svg on-success]
  (-> (.config ReactNativeBlobUtil (clj->js {:trusty platform/ios?}))
      (.fetch "GET" logo)
      (.then (fn [imgObj]
               (set-is-svg (= "image/svg"
                              (oops/oget imgObj
                                         ["respInfo" "headers" "Content-Type"])))
               (on-success (oops/oget imgObj "data"))))
      (.catch #(log/error "could not fetch favicon " logo))))

(defn- logo-comp
  [logo]
  (let [[image-data set-image-data] (rn/use-state nil)
        [is-svg? set-is-svg]        (rn/use-state nil)
        on-success                  (fn [data-uri]
                                      (set-image-data data-uri))
        _get-image-data             (get-image-data logo set-is-svg on-success)]
    (if is-svg?
      [svg/svg-xml (merge style/logo {:xml image-data})]
      [rn/image
       {:accessibility-label :logo
        :source              {:uri (str "data:image/png;base64," image-data)}
        :style               style/logo}])))

(defn view
  [{:keys [title logo description link thumbnail
           enabled? on-enable disabled-text
           container-style thumbnail-size]
    :or   {enabled? true}}]
  (let [theme (quo.theme/use-theme)]
    [rn/view
     {:style               (merge (style/container enabled? theme) container-style)
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
       [button-disabled disabled-text on-enable])]))
