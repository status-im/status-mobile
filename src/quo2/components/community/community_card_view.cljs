(ns quo2.components.community.community-card-view
  (:require [quo2.components.community.community-view :as community-view]
            [quo2.components.community.icon :as icon]
            [quo2.components.community.style :as style]
            [react-native.core :as rn]))

(defn community-card-view-item
  [{:keys [name description locked images cover
           status tokens tags width]} on-press]
  [rn/touchable-without-feedback
   {:accessibility-label :community-card-item
    :on-press            on-press}
   [rn/view {:style (style/community-card 20)}
    [rn/view
     {:style    {:width         width
                 :height        230
                 :border-radius 20}
      :on-press on-press}
     [rn/view
      {:flex 1}
      [rn/view (style/community-cover-container 60)
       [rn/image
        {:source cover
         :style
         {:flex                    1
          :border-top-right-radius 20
          :border-top-left-radius  20}}]]
      [rn/view (style/card-view-content-container 12)
       [rn/view (style/card-view-chat-icon 48)
        [icon/community-icon {:images images} 48]]
       (when (= status :gated)
         [rn/view (style/permission-tag-styles)
          [community-view/permission-tag-container
           {:locked locked
            :status status
            :tokens tokens}]])
       [community-view/community-title
        {:title       name
         :description description}]
       [rn/view {:style (style/card-stats-position)}
        [community-view/community-stats-column :card-view]]
       [rn/view {:style (style/community-tags-position)}
        [community-view/community-tags tags]]]]]]])

