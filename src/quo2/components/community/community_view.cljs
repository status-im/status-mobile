(ns quo2.components.community.community-view
  (:require [quo2.components.community.style :as style]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.components.tags.permission-tag :as permission]
            [quo2.components.tags.tag :as tag]
            [quo2.foundations.colors :as colors]
            [quo2.theme :as theme]
            [quo.gesture-handler :as gesture-handler]
            [react-native.core :as rn]
            [status-im.utils.money :as money]))

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
  [{:keys [type members-count active-count]}]
  (let [icon-color (colors/theme-colors colors/neutral-50 colors/neutral-40)]
    [rn/view
     (if (= type :card-view)
       (style/card-stats-container)
       (style/list-stats-container))
     [community-stats
      {:icon          :i/group
       :members-count (money/format-amount members-count)
       :icon-color    icon-color}]
     [community-stats
      {:icon          :i/lightning
       :members-count (money/format-amount active-count)
       :icon-color    icon-color}]]))

(defn community-tags
  [{:keys [tags container-style last-item-style]}]
  [gesture-handler/scroll-view
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

(defn- permission-tag-container-internal
  [{:keys [locked? blur? tokens on-press theme]}]
  [permission/tag
   {:background-color (if blur?
                        colors/white-opa-10
                        (colors/theme-colors colors/neutral-10 colors/neutral-80 theme))
    :locked?          locked?
    :tokens           tokens
    :size             24
    :on-press         on-press}])

(def permission-tag-container (theme/with-theme permission-tag-container-internal))
