(ns quo.components.status-link-previews.community.view
  (:require
    [quo.components.community.community-stat.view :as community-stat]
    [quo.components.links.link-preview.style :as style]
    [quo.components.markdown.text :as text]
    [quo.components.status-link-previews.community.style :as status-link-previews-style]
    [quo.theme :as theme]
    [react-native.core :as rn]
    [utils.re-frame :as rf]))

(defn- description-comp
  [description members-count active-members-count]
  [rn/view
   [text/text
    {:size                :paragraph-2
     :number-of-lines     3
     :accessibility-label :description} description]
   [rn/view {:flex-direction :row :margin-top 12}
    [community-stat/view
     {:value               members-count
      :icon                :i/members
      :accessibility-label :members-count
      :style               {:margin-right 12}}]
    [community-stat/view
     {:value               (or active-members-count 0)
      :icon                :i/active-members
      :accessibility-label :active-members-count}]]])

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
   {:style               status-link-previews-style/thumbnail
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

(defn- stat-loading
  [theme]
  [rn/view {:style status-link-previews-style/loading-stat-container}
   [rn/view {:style (status-link-previews-style/loading-circle theme)}]
   [rn/view {:style (status-link-previews-style/loading-stat theme)}]])

(defn- loading-view
  [theme]
  [rn/view
   {:accessibility-label :loading}
   [rn/view {:style status-link-previews-style/row-spacing}
    [rn/view (status-link-previews-style/loading-circle theme)]
    [rn/view (status-link-previews-style/loading-first-line-bar theme)]]
   [rn/view {:style (status-link-previews-style/loading-second-line-bar theme)}]
   [rn/view status-link-previews-style/row-spacing
    [stat-loading theme]
    [stat-loading theme]]
   [rn/view {:style (status-link-previews-style/loading-thumbnail-box theme)}]])

(defn f-internal-view
  [{:keys [title description link loading? thumbnail-size icon banner members-count active-members-count
           theme]
    :or   {loading? true}}]
  [rn/touchable-opacity
   {:style               (merge (style/container (not loading?))
                                {:width  295
                                 :height 245})
    :accessibility-label :link-preview
    :on-press            #(rf/dispatch [:universal-links/handle-url link])}
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
        [thumbnail-comp banner thumbnail-size])])])

(defn- internal-view
  [props]
  [:f> f-internal-view props])

(def view (theme/with-theme internal-view))
