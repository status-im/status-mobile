(ns quo2.components.community.community-list-view
  (:require
   [quo2.components.community.community-view :as community-view]
   [quo2.components.markdown.text :as text]
   [quo2.foundations.colors :as colors]
   [quo2.components.counter.counter :as counter]
   [quo2.components.icon :as icons]
   [status-im.communities.core :as communities]
   [status-im.utils.handlers :refer [>evt]]
   [status-im.ui.components.react :as react]
   [status-im.ui.screens.communities.styles :as styles]
   [status-im.ui.screens.communities.community :as community]
   [status-im.ui.screens.communities.icon :as communities.icon]))

(defn communities-list-view-item [{:keys [id name locked? status notifications
                                          tokens background-color] :as community}]
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
                   :ellipsize-mode      :tail
                   :style               {:color   (when (= notifications :muted)
                                                    (colors/theme-colors
                                                     colors/neutral-40
                                                     colors/neutral-60))}}
        name]
       [community-view/community-stats-column :list-view]]
      (if  (= status :gated)
        [community-view/permission-tag-container {:locked?      locked?
                                                  :tokens       tokens}]
        (cond
          (= notifications :unread-messages-count)
          [react/view {:style {:width            8
                               :height           8
                               :border-radius    4
                               :background-color (colors/theme-colors
                                                  colors/neutral-40
                                                  colors/neutral-60)}}]

          (= notifications :unread-mentions-count)
          [counter/counter {:type :default} 5]

          (= notifications :muted)
          [icons/icon  :main-icons2/muted {:container-style {:align-items     :center
                                                             :justify-content :center}
                                           :resize-mode      :center
                                           :size             20
                                           :color            (colors/theme-colors
                                                              colors/neutral-40
                                                              colors/neutral-50)}]))]]]])

(defn communities-membership-list-item [{:keys [id name status tokens locked?] :as community}]
  [react/view {:margin-bottom       20}
   [react/touchable-highlight {:underlay-color      colors/primary-50-opa-5
                               :style               {:border-radius 12}
                               :on-press      (fn []
                                                (>evt [::communities/load-category-states id])
                                                (>evt [:dismiss-keyboard])
                                                (>evt [:navigate-to :community {:community-id id}]))
                               :on-long-press #(>evt [:bottom-sheet/show-sheet
                                                      {:content (fn []
                                                                  [community/community-actions community])}])}
    [react/view {:flex               1}
     [react/view {:flex-direction    :row
                  :border-radius     16
                  :align-items       :center}
      [communities.icon/community-icon-redesign community 32]
      [react/view {:flex              1
                   :margin-left       12
                   :justify-content   :center}
       [text/text
        {:accessibility-label :chat-name-text
         :number-of-lines     1
         :ellipsize-mode      :tail
         :weight              :semi-bold
         :size                :paragraph-1}
        name]]
      (when (= status :gated)
        [react/view {:justify-content   :center
                     :margin-right      12}
         [community-view/permission-tag-container {:locked?      locked?
                                                   :tokens       tokens}]])]]]])