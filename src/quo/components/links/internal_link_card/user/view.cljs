(ns quo.components.links.internal-link-card.user.view
  (:require
    [quo.components.icon :as icon]
    [quo.components.links.internal-link-card.user.style :as style]
    [quo.components.markdown.text :as text]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.linear-gradient :as linear-gradient]))

(defn- subtitle-comp
  [subtitle emojis]
  [rn/view
   [text/text
    {:size                :paragraph-2
     :number-of-lines     2
     :accessibility-label :subtitle
     :style               {:margin-bottom 12}}
    subtitle]
   [rn/view {:style {:flex-direction :row}}
    (map-indexed (fn [index emoji]
                   ^{:key index}
                   [icon/icon emoji {:container-style {:margin-right 1}}])
                 emojis)]])

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
  [{:keys [title loading? icon
           theme on-press subtitle emojis customization-color]}]
  (if loading?
    [rn/pressable
     {:accessibility-label :internal-link-card
      :on-press            on-press
      :style               (style/container loading? theme)}
     [loading-view theme]]
    [linear-gradient/linear-gradient
     (assoc {:style (style/container loading? theme)}
            :colors
            (linear-gradient-props theme customization-color))
     [rn/pressable
      {:accessibility-label :internal-link-card
       :on-press            on-press}
      [rn/view {:style style/header-container}
       (when icon
         [logo-comp icon])
       [title-comp title]]
      (when subtitle
        [subtitle-comp subtitle emojis])]]))

(def view (quo.theme/with-theme view-internal))
