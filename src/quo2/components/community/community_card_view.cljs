(ns quo2.components.community.community-card-view
  (:require
   [quo2.components.community.community-view :as community-view]
   [quo2.components.community.style :as style]
   [react-native.core :as rn]))

(defn community-card-view-item
  [{:keys [name description locked
           status tokens cover tags featured]} on-press]
  [rn/touchable-opacity {:on-press on-press}
   [rn/view {:style (merge (style/community-card 20)
                           {:margin-bottom 16}
                           (if featured
                             {:margin-right 12}
                             {:flex              1
                              :margin-horizontal 20}))}
    [rn/view {:style    {:height        230
                         :border-radius 20}
              :on-press on-press}
     [rn/view
      {:flex 1}
      [rn/view (style/community-cover-container 40)
       [rn/image
        {:source cover
         :style
         {:flex          1
          :border-radius 20}}]]
      [rn/view (style/card-view-content-container 12)
       [rn/view (style/card-view-chat-icon 48)]
       ;;TODO new pure component based on quo2 should be implemented without status-im usage
       ;[communities.icon/community-icon-redesign community 48]]
       (when (= status :gated)
         [rn/view (style/permission-tag-styles)
          [community-view/permission-tag-container {:locked locked
                                                    :status status
                                                    :tokens tokens}]])
       [community-view/community-title
        {:title       name
         :description description}]
       [rn/view {:style (style/card-stats-position)}
        [community-view/community-stats-column :card-view]]
       [rn/view {:style (style/community-tags-position)}
        [community-view/community-tags tags]]]]]]])

