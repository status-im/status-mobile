(ns quo.components.community.community-card-view
  (:require
    [quo.components.community.community-view :as community-view]
    [quo.components.community.icon :as icon]
    [quo.components.community.style :as style]
    [quo.context :as quo.context]
    [react-native.core :as rn]))

(defn- loading-card-view
  [{:keys [width]}]
  (let [theme (quo.context/use-theme)]
    [rn/view {:style (style/loading-card width theme)}
     [rn/view (style/loading-cover-container theme)]
     [rn/view (style/loading-content-container theme)
      [rn/view (style/loading-avatar theme)]
      [rn/view (style/loading-lock theme)]
      [rn/view
       {:style style/loading-card-content-container}
       [rn/view {:style (style/loading-content-line {:theme theme :width 84 :margin-top 0})}]
       [rn/view {:style (style/loading-content-line {:theme theme :width 311 :margin-top 8})}]
       [rn/view {:style (style/loading-content-line {:theme theme :width 271 :margin-top 8})}]]
      [rn/view
       {:style style/loading-stats-container}
       [rn/view {:style (style/loading-stat-circle theme 0)}]
       [rn/view {:style (style/loading-stat-line theme 4)}]
       [rn/view {:style (style/loading-stat-circle theme 12)}]
       [rn/view {:style (style/loading-stat-line theme 4)}]]
      [rn/view
       {:style style/loading-tags-container}
       [rn/view {:style (style/loading-tag theme 0)}]
       [rn/view {:style (style/loading-tag theme 8)}]
       [rn/view {:style (style/loading-tag theme 8)}]]]]))

(defn- community-card-view
  [{:keys [community on-press width]}]
  (let [theme                                                              (quo.context/use-theme)
        {:keys [name description locked? images cover status tokens tags]} community]
    [rn/pressable
     {:accessibility-label :community-card-item
      :on-press            on-press
      :style               (style/community-card 20 theme)}
     [rn/view
      {:style    {:width         width
                  :height        230
                  :border-radius 20}
       :on-press on-press}
      [rn/view {:style style/detail-container}
       [rn/view (style/community-cover-container 60)
        [rn/image
         {:source cover
          :style  {:flex                    1
                   :border-top-right-radius 20
                   :border-top-left-radius  20}}]]
       [rn/view (style/card-view-content-container 12 theme)
        [rn/view (style/card-view-chat-icon 48 theme)
         [icon/community-icon {:images images} 48]]
        (when (= status :gated)
          [rn/view style/permission-tag-styles
           [community-view/permission-tag-container
            {:locked? locked?
             :status  status
             :tokens  tokens}]])
        [community-view/community-title
         {:title       name
          :description description}]
        [rn/view {:style style/card-stats-position}
         [community-view/community-stats-column
          {:type                 :card-view
           :members-count        (:members-count community)
           :active-members-count (:active-members-count community)}]]
        [rn/view {:style style/community-tags-position}
         [community-view/community-tags {:tags tags}]]]]]]))

(defn view
  [{:keys [loading?] :as props}]
  (if-not loading?
    [community-card-view props]
    [loading-card-view props]))
