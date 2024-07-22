(ns quo.components.links.internal-link-card.user.view
  (:require
    [quo.components.links.internal-link-card.schema :as component-schema]
    [quo.components.links.internal-link-card.user.style :as style]
    [quo.components.markdown.text :as text]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]
    [schema.core :as schema]))

(defn- subtitle-comp
  [subtitle emoji-hash]
  [rn/view
   [text/text
    {:size                :paragraph-2
     :number-of-lines     2
     :accessibility-label :subtitle
     :style               {:margin-bottom 12}}
    subtitle]
   [rn/view {:style {:flex-direction :row}}
    [text/text
     {:size                :paragraph-2
      :number-of-lines     1
      :weight              :regular
      :accessibility-label :emoji-hash}
     emoji-hash]]])

(defn- title-comp
  [title]
  [text/text
   {:size                :paragraph-1
    :number-of-lines     1
    :weight              :semi-bold
    :style               style/title
    :accessibility-label :title}
   title])

(defn- logo-comp
  [logo]
  [rn/image
   {:accessibility-label :logo
    :source              logo
    :style               style/logo}])

(defn- loading-view
  [theme]
  [rn/view {:accessibility-label :loading-user-link-view}
   [rn/view {:style style/row-spacing}
    [rn/view {:style (style/loading-circle theme)}]
    [rn/view {:style (style/loading-first-line-bar theme)}]]
   [rn/view {:style (style/loading-second-line-bar theme)}]
   [rn/view {:style (style/last-bar-line-bar theme)}]])

(defn- linear-gradient-props
  [theme customization-color]
  [(style/gradient-start-color customization-color theme) :transparent])

(defn view-internal
  [{:keys [title loading? icon on-press subtitle emoji-hash customization-color size]}]
  (let [theme (quo.theme/use-theme)]
    (if loading?
      [rn/pressable
       {:accessibility-label :internal-link-card
        :on-press            on-press
        :style               (style/container loading? theme size)}
       [loading-view theme]]
      [linear-gradient/linear-gradient
       {:style  (style/container loading? theme size)
        :colors (linear-gradient-props theme customization-color)}
       [rn/pressable
        {:accessibility-label :internal-link-card
         :on-press            on-press}
        [rn/view {:style style/header-container}
         (when icon
           [logo-comp icon])
         [title-comp title]]
        (when subtitle
          [subtitle-comp subtitle emoji-hash])]])))

(def view (schema/instrument #'view-internal component-schema/?schema))
