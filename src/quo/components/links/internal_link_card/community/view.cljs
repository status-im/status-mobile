(ns quo.components.links.internal-link-card.community.view
  (:require
    [quo.components.community.community-stat.view :as community-stat]
    [quo.components.links.internal-link-card.community.style :as style]
    [quo.components.links.internal-link-card.schema :as component-schema]
    [quo.components.markdown.text :as text]
    [quo.context :as quo.context]
    [react-native.core :as rn]
    [schema.core :as schema]))

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
      :style               {:margin-right 12}
      :text-size           :paragraph-2}]
    (when active-members-count
      [community-stat/view
       {:value               active-members-count
        :icon                :i/active-members
        :accessibility-label :active-members-count
        :text-size           :paragraph-2}])]])

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
  [theme size]
  [rn/view {:accessibility-label :loading-community-link-view}
   [rn/view {:style style/row-spacing}
    [rn/view {:style (style/loading-circle theme)}]
    [rn/view {:style (style/loading-first-line-bar theme)}]]
   [rn/view {:style (style/loading-second-line-bar theme)}]
   [rn/view {:style style/row-spacing}
    [stat-loading theme]
    [stat-loading theme]]
   [rn/view {:style (style/loading-thumbnail-box theme size)}]])

(defn- view-internal
  [{:keys [title description loading? icon banner members-count active-members-count
           on-press size]}]
  (let [theme (quo.context/use-theme)]
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
         [title-comp title]]
        (when description
          [description-comp description members-count active-members-count])
        (when banner
          [thumbnail-comp banner size])])]))

(def view (schema/instrument #'view-internal component-schema/?schema))
