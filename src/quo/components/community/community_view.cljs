(ns quo.components.community.community-view
  (:require
    [quo.components.community.community-stat.view :as community-stat]
    [quo.components.community.style :as style]
    [quo.components.markdown.text :as text]
    [quo.components.tags.permission-tag :as permission]
    [quo.components.tags.tag :as tag]
    [quo.foundations.colors :as colors]
    [quo.theme]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]))

(defn community-stats-column
  [{:keys [type members-count active-members-count]}]
  [rn/view (if (= type :card-view) style/card-stats-container style/list-stats-container)
   [community-stat/view
    {:accessibility-label :stats-members-count
     :icon                :i/group
     :value               members-count
     :style               {:margin-right 12}}]
   [community-stat/view
    {:accessibility-label :stats-active-count
     :icon                :i/active-members
     :value               active-members-count}]])

(defn community-tags
  [{:keys [tags container-style last-item-style]}]
  [gesture/scroll-view
   {:shows-horizontal-scroll-indicator false
    :horizontal                        true
    :style                             container-style}
   (let [last-index (max 0 (dec (count tags)))]
     (map-indexed
      (fn [index {tag-name :name emoji :emoji}]
        (let [last? (= index last-index)]
          [rn/view
           {:key   tag-name
            :style (if last?
                     last-item-style
                     {:margin-right 8})}
           [tag/tag
            {:size      24
             :label     tag-name
             :type      :emoji
             :labelled? true
             :resource  emoji}]]))
      tags))])

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
  [{:keys [locked? blur? tokens on-press]}]
  (let [theme (quo.theme/use-theme)]
    [permission/tag
     {:accessibility-label :permission-tag
      :background-color    (if (and (= :dark theme) blur?)
                             colors/white-opa-10
                             (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))
      :locked?             locked?
      :tokens              tokens
      :size                24
      :on-press            on-press}]))
