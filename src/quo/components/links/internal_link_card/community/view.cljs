(ns quo.components.links.internal-link-card.community.view
  (:require
    [quo.components.community.community-stat.view :as community-stat]
    [quo.components.links.internal-link-card.community.style :as style]
    [quo.components.markdown.text :as text]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn- description-comp
  [description members-count active-members-count]
  [rn/view
   [text/text
    {:size                :paragraph-2
     :number-of-lines     3
     :accessibility-label :description}
    description]
   [rn/view {:style style/stat-container}
    [community-stat/view
     {:value               members-count
      :icon                :i/members
      :accessibility-label :members-count
      :style               {:margin-right 12}}]
    (when active-members-count
      [community-stat/view
       {:value               active-members-count
        :icon                :i/active-members
        :accessibility-label :active-members-count}])]])

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
  [thumbnail]
  [rn/image
   {:style               style/thumbnail
    :source              thumbnail
    :accessibility-label :thumbnail}])

(defn- logo-comp
  [logo]
  [rn/image
   {:accessibility-label :logo
    :source              logo
    :style               style/logo}])

(defn- stat-loading
  [theme]
  [rn/view {:style style/loading-stat-container}
   [rn/view {:style (style/loading-circle theme)}]
   [rn/view {:style (style/loading-stat theme)}]])

(defn- loading-view
  [theme]
  [rn/view {:accessibility-label :loading-community-link-view}
   [rn/view {:style style/row-spacing}
    [rn/view {:style (style/loading-circle theme)}]
    [rn/view {:style (style/loading-first-line-bar theme)}]]
   [rn/view {:style (style/loading-second-line-bar theme)}]
   [rn/view {:style style/row-spacing}
    [stat-loading theme]
    [stat-loading theme]]
   [rn/view {:style (style/loading-thumbnail-box theme)}]])

(defn- internal-view
  [{:keys [title description loading? icon banner members-count active-members-count
           theme on-press]}]
  [rn/pressable
   {:style               (style/container theme)
    :accessibility-label :internal-link-card
    :on-press            on-press}
   (if loading?
     [loading-view theme]
     [:<>
      [rn/view {:style style/header-container}
       (when icon
         [logo-comp icon])
       [title-comp title]]
      (when description
        [description-comp description members-count active-members-count])
      (when banner
        [thumbnail-comp banner])])])

(def view (quo.theme/with-theme internal-view))
