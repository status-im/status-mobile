(ns quo2.components.communities.community-card-view
  (:require
   [quo2.components.communities.community-view :as community-view]
   [status-im.communities.core :as communities]
   [status-im.utils.handlers :refer [>evt <sub]]
   [status-im.ui.components.react :as react]
   [status-im.ui.screens.communities.styles :as styles]
   [status-im.ui.screens.communities.community :as community]
   [status-im.ui.screens.communities.icon :as communities.icon]))

(defn community-card-view-item [{:keys [id name description locked
                                        status tokens cover tags featured] :as community}]
  (let [width (* (<sub [:dimensions/window-width]) 0.90)]
    [react/view {:style (merge (styles/community-card 20)
                               {:margin-bottom 16}
                               (if featured
                                 {:margin-right 12
                                  :width width}
                                 {:flex              1
                                  :margin-horizontal 20}))}
     [react/view {:style         {:height          230
                                  :border-radius   20}
                  :on-press      (fn []
                                   (>evt [::communities/load-category-states id])
                                   (>evt [:dismiss-keyboard])
                                   (>evt [:navigate-to :community {:community-id id}]))
                  :on-long-press #(>evt [:bottom-sheet/show-sheet
                                         {:content (fn [] [community/community-actions community])}])}
      [react/view
       {:flex 1}
       [react/view (styles/community-cover-container 40)
        [react/image
         {:source cover
          :style
          {:flex 1
           :border-radius 20}}]]
       [react/view (styles/card-view-content-container 12)
        [react/view (styles/card-view-chat-icon 48)
         [communities.icon/community-icon-redesign community 48]]
        (when (= status :gated)
          [react/view (styles/permission-tag-styles)
           [community-view/permission-tag-container {:locked locked
                                                     :status status
                                                     :tokens tokens}]])
        [community-view/community-title
         {:title name
          :description description}]
        [community-view/community-stats-column :card-view]
        [community-view/community-tags tags]]]]]))