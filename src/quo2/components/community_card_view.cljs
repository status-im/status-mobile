(ns quo2.components.community-card-view
  (:require
   [quo2.components.text :as text]
   [quo2.components.icon :as icons]
   [quo2.foundations.colors :as colors]
   [quo2.components.permission-tag :as permission]
   [quo2.components.filter-tag  :as filter-tag]
   [status-im.communities.core :as communities]
   [status-im.utils.handlers :refer [>evt <sub]]
   [status-im.ui.components.react :as react]
   [status-im.utils.money :as money]
   [status-im.i18n.i18n :as i18n]
   [status-im.ui.screens.communities.styles :as styles]
   [status-im.ui.screens.communities.community :as community]
   [status-im.ui.screens.communities.icon :as communities.icon]))

(defn format-members [count]
  (if (> count 1000000)
    (str (money/with-precision (/ count 1000000) 1) (i18n/label :t/M))
    (if (and (> count 999) (< count 1000000))
      (str (money/with-precision (/ count 1000) 1) (i18n/label :t/K))
      count)))

(defn community-stats [{:keys [icon count icon-color]}]
  [react/view (styles/stats-count-container)
   [react/view {:margin-right  4}
    [icons/icon icon {:container-style {:align-items     :center
                                        :justify-content :center}
                      :resize-mode      :center
                      :size             16
                      :color            icon-color}]]
   [text/text {:weight  :regular
               :size    :paragraph-1}
    (format-members count)]])

(defn community-stats-column [type]
  (let [icon-color  (colors/theme-colors colors/neutral-50 colors/neutral-40)]
    [react/view (if (= type :card-view)
                  (styles/card-stats-container)
                  (styles/list-stats-container))
     [community-stats {:icon       :main-icons2/group
                       :count      630000
                       :icon-color icon-color}]
     [community-stats {:icon       :main-icons2/lightning
                       :count      3300
                       :icon-color icon-color}]
     [community-stats {:icon       :main-icons2/placeholder
                       :count      63
                       :icon-color icon-color}]]))

(defn community-tags [tags]
  [react/view (styles/community-tags-container)
   (for [{:keys [id tag-label resource]} tags]
     ^{:key id}
     [react/view {:margin-right 8}
      [filter-tag/filter-tag
       {:id          id
        :size        24
        :label       tag-label
        :type        :emoji
        :labelled    true
        :resource    resource}]])])

(defn community-title [{:keys [title description]}]
  [react/view (styles/community-title-description-container)
   (when title
     [text/text
      {:accessibility-label :chat-name-text
       :number-of-lines     1
       :ellipsize-mode      :tail
       :weight              :semi-bold
       :size                :heading-2}
      title])
   (when description
     [text/text
      {:accessibility-label :community-description-text
       :number-of-lines     2
       :ellipsize-mode      :tail
       :weight  :regular
       :size    :paragraph-1}
      description])])

(defn permission-tag-container [{:keys [locked tokens]}]
  [permission/tag {:background-color (colors/theme-colors
                                      colors/neutral-10
                                      colors/neutral-80)
                   :locked           locked
                   :tokens           tokens
                   :size             24}])

(defn community-card-view-item [{:keys [id name description locked
                                        status tokens cover tags featured] :as community}]
  (let [width (* (<sub [:dimensions/window-width]) 0.90)]
    [react/view {:style (merge (styles/community-card 20)
                               {:margin-bottom      16}
                               (if featured
                                 {:margin-right      12
                                  :width             width}
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
      [react/view {:flex    1}
       [react/view (styles/community-cover-container)
        [react/image {:source      cover
                      :style  {:flex            1
                               :border-radius   20}}]]
       [react/view (styles/card-view-content-container)
        [react/view (styles/card-view-chat-icon)
         [communities.icon/community-icon-redesign community 48]]
        (when (= status :gated)
          [react/view (styles/permission-tag-styles)
           [permission-tag-container {:locked       locked
                                      :status       status
                                      :tokens       tokens}]])
        [community-title {:title       name
                          :description description}]
        [community-stats-column :card-view]
        [community-tags tags]]]]]))

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
       [community-stats-column :list-view]]
      (when (= status :gated)
        [permission-tag-container {:locked       locked
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
       [community-title {:title  name}]]
      (when (= status :gated)
        [react/view {:justify-content   :center
                     :margin-right      12}
         [permission-tag-container {:locked       locked
                                    :status       status
                                    :tokens       tokens}]])]]]])
