(ns status-im.ui.screens.communities.community-views-redesign
  (:require
   [quo2.components.text :as text]
   [status-im.ui.screens.communities.styles :as styles]
   [quo2.foundations.typography :as typography]
   [quo2.components.icon :as icons]
   [quo2.foundations.colors :as quo2.colors]
   [quo2.components.permission-tag :as permission]
   [quo2.components.filter-tag  :as filter-tag]
   [status-im.communities.core :as communities]
   [status-im.utils.handlers :refer [>evt <sub]]
   [status-im.ui.components.react :as react]
   [status-im.i18n.i18n :as i18n]
   [status-im.ui.screens.communities.community :as community]
   [status-im.ui.screens.communities.icon :as communities.icon]))

(def icon-color (quo2.colors/theme-colors
                 quo2.colors/neutral-50
                 quo2.colors/neutral-40))

(defn community-stats [icon count]
  [react/view (styles/stats-count-container)
   [react/view {:margin-right  4}
    [icons/icon icon {:container-style {:align-items     :center
                                        :justify-content :center}
                      :resize-mode      :center
                      :size             16
                      :color            icon-color}]]
   [text/text {:weight  :regular
               :size    :paragraph-1}
    (i18n/format-members count)]])

(defn community-stats-column [styles]
  [react/view styles
   [community-stats :main-icons2/group 630000]
   [community-stats :main-icons2/lightning 3300]
   [community-stats :main-icons2/placeholder 63]])

(defn community-tags [tags]
  [react/view (styles/community-tags-container)
   (for [{:keys [id label emoji]} tags]
     ^{:key id}
     [react/view {:margin-right 8}
      [filter-tag/tag
       {:id          id
        :size        24
        :with-label  true
        :type        :emoji
        :emoji       emoji}
       label]])])

(defn community-card-view-item [{:keys [id name description access token-groups cover
                                        status tags section] :as community}]
  (let [window-width (* (<sub [:dimensions/window-width]) 0.91)
        bg-color (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)]
    [react/view {:style (merge (styles/community-card window-width bg-color)
                               {:margin-bottom  16}
                               (when (= section "featured")
                                 {:margin-right       12
                                  :width              window-width}))}
     [react/touchable-opacity {:style         (merge {:height               230
                                                      :border-radius        20})
                               :on-press      (fn []
                                                (>evt [::communities/load-category-states id])
                                                (>evt [:dismiss-keyboard])
                                                (>evt [:navigate-to :community {:community-id id}]))
                               :on-long-press #(>evt [:bottom-sheet/show-sheet
                                                      {:content (fn []
                                                                  [community/community-actions community])}])}
      [:<>
       [react/view (styles/community-cover-image-container)
        [react/image {:source cover
                      :style  {:width        :100%
                               :margin-right 4}}]]
       [react/view (styles/card-view-content-container bg-color)
        [react/view (styles/card-view-chat-icon bg-color)
         [communities.icon/community-icon-redesign community 48]]
        [react/view {:position         :absolute
                     :top              8
                     :right            8}
         (when (= status "gated")
           [permission/permission-tag {:icon             (if access :main-icons2/unlocked :main-icons2/locked)
                                       :background-color (quo2.colors/theme-colors
                                                          quo2.colors/neutral-10
                                                          quo2.colors/neutral-80)
                                       :icon-color       icon-color
                                       :token-groups     token-groups
                                       :size             24}])]
        [react/view (styles/community-title-description-container)
         [text/text
          {:accessibility-label :chat-name-text
           :number-of-lines     1
           :ellipsize-mode      :tail
           :weight              :semi-bold
           :size                :heading-2}
          name]
         [text/text {:accessibility-label :community-description-text
                     :number-of-lines     2
                     :ellipsize-mode      :tail
                     :weight  :regular
                     :size    :paragraph-1}

          description]]
        [community-stats-column (styles/card-stats-container)]
        [community-tags tags]]]]]))

(defn communities-list-view-item [{:keys [id name status token-groups access] :as community}]
  (let [bg-color (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)
        window-width (* (<sub [:dimensions/window-width]) 0.91)]
    [react/view {:style (merge (styles/community-card window-width bg-color)
                               {:margin-bottom  12})}
     [react/touchable-opacity {:border-radius   20
                               :on-press      (fn []
                                                (>evt [::communities/load-category-states id])
                                                (>evt [:dismiss-keyboard])
                                                (>evt [:navigate-to :community {:community-id id}]))
                               :on-long-press #(>evt [:bottom-sheet/show-sheet
                                                      {:content (fn []
                                                                  [community/community-actions community])}])}
      [react/view {:flex               1
                   :justify-content    :center}
       [react/view (styles/list-view-content-container bg-color)
        [react/view (styles/list-view-chat-icon bg-color)
         [communities.icon/community-icon-redesign community 48]]
        [react/view {:flex 1}
         [text/text
          {:style (merge
                   typography/font-semi-bold
                   typography/heading-2)
           :accessibility-label :chat-name-text
           :number-of-lines     1
           :ellipsize-mode      :tail}
          name]
         [community-stats-column (styles/list-stats-container)]]
        (when (= status "gated")
          [react/view {:margin-right   12}
           [permission/permission-tag {:background-color (quo2.colors/theme-colors
                                                          quo2.colors/neutral-10
                                                          quo2.colors/neutral-80)
                                       :icon             (if access :main-icons2/unlocked :main-icons2/locked)
                                       :icon-color       icon-color
                                       :token-groups     token-groups
                                       :size             24}]])]]]]))

(defn communities-membership-list-item [{:keys [id name status tokens access] :as community}]
  [react/view {:margin-bottom  12
               :padding-horizontal  8}
   [react/touchable-highlight {:underlay-color      quo2.colors/primary-50-opa-5
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
       [text/text
        {:style (merge
                 typography/font-semi-bold
                 typography/heading-2)
         :accessibility-label :chat-name-text
         :number-of-lines     1
         :ellipsize-mode      :tail}
        name]]
      [react/view
       (when (= status "gated")
         [permission/permission-tag {:background-color (quo2.colors/theme-colors
                                                        quo2.colors/neutral-10
                                                        quo2.colors/neutral-80)
                                     :icon             (if access :main-icons2/unlocked :main-icons2/locked)
                                     :icon-color       icon-color
                                     :tokens           tokens
                                     :size             24}])]]]]])
