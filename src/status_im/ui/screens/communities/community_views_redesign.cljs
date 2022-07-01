(ns status-im.ui.screens.communities.community-views-redesign
  (:require
   [quo2.components.text :as text]
   [status-im.ui.screens.communities.styles :as styles]
   [quo.design-system.spacing :as spacing]
   [quo2.components.filter-tag :as quo2.filter-tag]
   [quo2.foundations.typography :as typography]
   [quo2.components.icon :as icons]
   [quo2.foundations.colors :as quo2.colors]
   [status-im.i18n.i18n :as i18n]
   [status-im.communities.core :as communities]
   [status-im.utils.handlers :refer [>evt <sub]]
   [status-im.ui.components.react :as react]
   [status-im.ui.screens.communities.community :as community]
   [status-im.ui.screens.communities.icon :as communities.icon]))

(defn community-stats [styles]
  [react/view {:style styles}
   [react/view {:style (styles/stats-count-container)}
    [icons/icon :main-icons2/group {:container-style {:align-items     :center
                                                      :justify-content :center}
                                    :resize-mode      :center
                                    :size             16
                                    :color            (quo2.colors/theme-colors
                                                       quo2.colors/neutral-50
                                                       quo2.colors/neutral-40)}]
    [text/text {:style (merge typography/font-regular
                              typography/paragraph-1)}
     (i18n/format-members 630000)]]
   [react/view {:style (styles/stats-count-container)}
    [icons/icon :main-icons2/lightning {:container-style {:align-items     :center
                                                          :justify-content :center}
                                        :resize-mode      :center
                                        :size             16
                                        :color            (quo2.colors/theme-colors
                                                           quo2.colors/neutral-50
                                                           quo2.colors/neutral-40)}]
    [text/text {:style (merge typography/font-regular
                              typography/paragraph-1)}
     (i18n/format-members 3300)]]
   [react/view {:style (styles/stats-count-container)}
    [icons/icon :main-icons2/placeholder {:container-style {:align-items     :center
                                                            :justify-content :center}
                                          :resize-mode      :center
                                          :size             16
                                          :color            (quo2.colors/theme-colors
                                                             quo2.colors/neutral-50
                                                             quo2.colors/neutral-40)}]
    [text/text {:style (merge typography/font-regular
                              typography/paragraph-1)}
     (i18n/format-members 63)]]])

(defn community-tags [tags]
  [react/view {:style (styles/community-tags-container)}
   (for [{:keys [id label resource]} tags]
     ^{:key id}
     [react/view {:margin-right 8}
      [quo2.filter-tag/tag
       {:id       id
        :size     24
        :resource resource}
       label]])])

(defn permissions-tag [permissions community status color view]
  (when (= status "gated")
    [react/view {:style (styles/permission-tag-container color view)}
     [react/view {:style {:padding-right (:x-tiny spacing/spacing)}}
      [icons/icon (if permissions :main-icons2/unlocked :main-icons2/locked)
       {:container-style {:align-items     :center
                          :justify-content :center}
        :resize-mode      :center
        :size             16
        :color            (quo2.colors/theme-colors
                           quo2.colors/neutral-50
                           quo2.colors/neutral-40)}]]
     [communities.icon/community-icon-redesign community 20]]))

(defn community-card-list-item [{:keys [id name description permissions
                                        status tags section] :as community}]
  (let [theme-color         (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)
        status-bg-color (quo2.colors/theme-colors quo2.colors/neutral-10 quo2.colors/neutral-80)
        window-width (* (<sub [:dimensions/window-width]) 0.91)]
    [react/view {:style (merge (styles/community-card window-width theme-color)
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
       [react/view {:style (styles/community-cover-image-container)}]
       [react/view {:style (styles/community-card-content-container theme-color)}
        [react/view {:style (styles/community-card-chat-icon theme-color)}
         [communities.icon/community-icon-redesign community 48]]
        [permissions-tag permissions community status status-bg-color :card-view]
        [react/view {:style (styles/community-title-description-container)}
         [text/text
          {:style (merge
                   typography/font-semi-bold
                   typography/heading-2)
           :accessibility-label :chat-name-text
           :number-of-lines     1
           :ellipsize-mode      :tail}
          name]
         [text/text
          {:style (merge typography/font-regular
                         typography/paragraph-1)
           :accessibility-label :community-description-text
           :number-of-lines     2
           :ellipsize-mode      :tail}
          description]]
        [community-stats (styles/card-stats-container)]
        [community-tags tags]]]]]))

(defn categorized-communities-list-item [{:keys [id name status] :as community}]
  (let [card-bg-color (quo2.colors/theme-colors quo2.colors/white quo2.colors/neutral-90)
        status-bg-color (quo2.colors/theme-colors quo2.colors/neutral-10 quo2.colors/neutral-80)
        permissions  false
        window-width (* (<sub [:dimensions/window-width]) 0.91)]
    [react/view {:style (merge (styles/community-card window-width card-bg-color)
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
       [react/view {:flex-direction    :row
                    :border-radius     16
                    :align-items       :center
                    :background-color  card-bg-color}
        [react/view {:border-radius    32
                     :padding          12
                     :background-color card-bg-color}
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
         [community-stats (styles/list-stats-container)]]
        [permissions-tag permissions community status status-bg-color :list-view]]]]]))

(defn communities-membership-list-item [{:keys [id name status] :as community}]
  (let [permissions  false
        status-bg-color (quo2.colors/theme-colors quo2.colors/neutral-20 quo2.colors/neutral-80)]
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
                     :flex               1}
         [text/text
          {:style (merge
                   typography/font-semi-bold
                   typography/heading-2)
           :accessibility-label :chat-name-text
           :number-of-lines     1
           :ellipsize-mode      :tail}
          name]]
        [permissions-tag permissions community status status-bg-color :list-view]]]]]))
