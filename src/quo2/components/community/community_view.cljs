(ns quo2.components.community.community-view
  (:require [quo2.components.community.style :as style]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.tags.permission-tag :as permission]
            [quo2.components.tags.tag :as tag]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]))

(defn community-stats
  [{:keys [icon members-count icon-color]}]
  [rn/view (style/stats-count-container)
   [rn/view {:margin-right 4}
    [icons/icon icon
     {:container-style {:align-items     :center
                        :justify-content :center}
      :resize-mode     :center
      :size            16
      :color           icon-color}]]
   [text/text
    {:weight :regular
     :size   :paragraph-1}
    members-count]])

(defn community-stats-column
  [{:keys [type]}]
  (let [icon-color (colors/theme-colors colors/neutral-50 colors/neutral-40)]
    [rn/view
     (if (= type :card-view)
       (style/card-stats-container)
       (style/list-stats-container))
     [community-stats
      {:icon          :i/group
       :members-count "629.2K" ;;TODO here should be formatted value, use money/format-members from
                               ;;outside this component
       :icon-color    icon-color}]
     [community-stats
      {:icon          :i/lightning
       :members-count "112.1K"
       :icon-color    icon-color}]]))

(defn community-tags
  [tags]
  [rn/view (style/community-tags-container)
   (for [{:keys [name emoji]} tags]
     ^{:key name}
     [rn/view {:margin-right 8}
      [tag/tag
       {:size        24
        :label       name
        :type        :emoji
        :labelled?   true
        :scrollable? true
        :resource    emoji}]])])

(defn community-title
  [{:keys [title description size] :or {size :small}}]
  [rn/view (style/community-title-description-container (if (= size :large) 56 32))
   (when title
     [text/text
      {:accessibility-label :chat-name-text
       :number-of-lines     1
       :ellipsize-mode      :tail
       :weight              :semi-bold
       :size                (if (= size :large) :heading-1 :heading-2)}
      title])
   (when description
     [text/text
      {:accessibility-label :community-description-text
       :number-of-lines     2
       :ellipsize-mode      :tail
       :weight              :regular
       :size                :paragraph-1
       :style               {:margin-top (if (= size :large) 8 2)}}
      description])])

(defn permission-tag-container
  [{:keys [locked? tokens on-press]}]
  [permission/tag
   {:background-color (colors/theme-colors
                       colors/neutral-10
                       colors/neutral-80)
    :locked?          locked?
    :tokens           tokens
    :size             24
    :on-press         on-press}])
