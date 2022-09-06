(ns quo2.components.communities.community-list-view
  (:require
   [quo2.components.communities.community-view :as community-view]
   [quo2.components.text :as text]
   [quo2.foundations.colors :as colors]
   [status-im.communities.core :as communities]
   [status-im.utils.handlers :refer [>evt]]
   [status-im.ui.components.react :as react]
   [status-im.ui.screens.communities.styles :as styles]
   [status-im.ui.screens.communities.community :as community]
   [status-im.ui.screens.communities.icon :as communities.icon]))

(defn communities-list-view-item [{:keys [id name locked status tokens background-color] :as community}]
  [react/view {:style (merge (styles/community-card 16)
                             {:margin-bottom    12
                              :margin-horizontal 20})}
   [react/view {:style         {:height             56
                                :border-radius      16}
                :on-press      (fn []
                                 (>evt [::communities/load-category-states id])
                                 (>evt [:dismiss-keyboard])
                                 (>evt [:navigate-to :community {:community-id id}]))
                :on-long-press #(>evt [:bottom-sheet/show-sheet
                                       {:content (fn []
                                                   [community/community-actions community])}])}
    [react/view {:flex    1}
     [react/view {:flex-direction     :row
                  :border-radius      16
                  :padding-horizontal 12
                  :align-items        :center
                  :padding-vertical   8
                  :background-color   background-color}
      [react/view
       [communities.icon/community-icon-redesign community 32]]
      [react/view {:flex              1
                   :margin-horizontal 12}
       [text/text {:weight              :semi-bold
                   :size                :paragraph-1
                   :accessibility-label :community-name-text
                   :number-of-lines     1
                   :ellipsize-mode      :tail}
        name]
       [community-view/community-stats-column :list-view]]
      (when (= status :gated)
        [community-view/permission-tag-container {:locked       locked
                                                  :status       status
                                                  :tokens       tokens}])]]]])

(defn communities-membership-list-item [{:keys [id name status tokens locked] :as community}]
  [react/view {:margin-bottom       12
               :padding-horizontal  8}
   [react/touchable-highlight {:underlay-color      colors/primary-50-opa-5
                               :style               {:border-radius 12}
                               :on-press      (fn []
                                                (>evt [::communities/load-category-states id])
                                                (>evt [:dismiss-keyboard])
                                                (>evt [:navigate-to :community {:community-id id}]))
                               :on-long-press #(>evt [:bottom-sheet/show-sheet
                                                      {:content (fn []
                                                                  [community/community-actions community])}])}
    [react/view {:flex               1
                 :padding-vertical   8
                 :padding-horizontal 12}
     [react/view {:flex-direction    :row
                  :border-radius     16
                  :align-items       :center}
      [communities.icon/community-icon-redesign community 48]
      [react/view {:margin-left   12
                   :flex          1}
       [community-view/community-title {:title  name}]]
      (when (= status :gated)
        [react/view {:justify-content   :center
                     :margin-right      12}
         [community-view/permission-tag-container {:locked       locked
                                                   :status       status
                                                   :tokens       tokens}]])]]]])