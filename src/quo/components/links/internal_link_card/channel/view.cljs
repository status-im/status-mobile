(ns quo.components.links.internal-link-card.channel.view
  (:require
    [quo.components.icon :as icon]
    [quo.components.links.internal-link-card.channel.style :as style]
    [quo.components.links.internal-link-card.schema :as component-schema]
    [quo.components.markdown.text :as text]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [schema.core :as schema]))

(defn- description-comp
  [description]
  [rn/view {:style {:margin-bottom 4}}
   [text/text
    {:size                :paragraph-2
     :number-of-lines     3
     :accessibility-label :description}
    description]])

(defn- title-comp
  [title channel-name theme]
  [rn/view
   {:style {:flex-direction :row
            :align-items    :center}}
   [text/text
    {:size                :paragraph-1
     :number-of-lines     1
     :weight              :semi-bold
     :style               style/title
     :accessibility-label :title}
    title]
   [icon/icon :i/chevron-right (style/channel-chevron-props theme)]
   [text/text
    {:size                :paragraph-1
     :number-of-lines     1
     :weight              :semi-bold
     :style               style/title
     :accessibility-label :title}
    channel-name]])

(defn- banner-comp
  [thumbnail size]
  [rn/image
   {:style               (style/thumbnail size)
    :source              thumbnail
    :accessibility-label :banner}])

(defn- logo-comp
  [logo]
  [rn/image
   {:accessibility-label :logo
    :source              logo
    :style               (assoc style/logo :margin-bottom 2)}])

(defn- loading-view
  [theme size]
  [rn/view
   {:accessibility-label :loading-channel-link-view
    :style               {:height 215}}
   [rn/view {:style {:flex-direction :row}}
    [rn/view {:style style/row-spacing}
     [rn/view {:style (style/loading-circle theme)}]
     [rn/view {:style (style/loading-first-line-bar theme true)}]]
    [rn/view {:style style/row-spacing}
     [rn/view {:style (style/loading-circle theme)}]
     [rn/view {:style (style/loading-first-line-bar theme false)}]]]
   [rn/view {:style (style/loading-second-line-bar theme)}]
   [rn/view {:style (style/loading-thumbnail-box theme size)}]])

(defn view-internal
  [{:keys [title description loading? icon banner
           theme on-press channel-name size]
    :or   {channel-name "empty name"}}]
  [rn/pressable
   {:style               (style/container size theme)
    :accessibility-label :internal-link-card
    :on-press            on-press}
   (if loading?
     [loading-view theme size]
     [:<>
      [rn/view {:style style/header-container}
       (when icon
         [logo-comp icon])
       [title-comp title channel-name theme]]
      (when description
        [description-comp description])
      (when banner
        [banner-comp banner size])])])

(def view
  (quo.theme/with-theme
   (schema/instrument #'view-internal component-schema/?schema)))
