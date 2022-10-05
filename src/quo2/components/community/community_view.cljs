(ns quo2.components.community.community-view
  (:require
   [quo2.components.markdown.text :as text]
   [quo2.components.icon :as icons]
   [quo2.foundations.colors :as colors]
   [quo2.components.tags.permission-tag :as permission]
   [quo2.components.tags.tag  :as tag]
   [status-im.ui.components.react :as react]
   [status-im.utils.money :as money]
   [status-im.i18n.i18n :as i18n]
   [status-im.ui.screens.communities.styles :as styles]))

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
                       :count      "629.2K"
                       :icon-color icon-color}]
     [community-stats {:icon       :main-icons2/lightning
                       :count      "112.1K"
                       :icon-color icon-color}]
     [community-stats {:icon       :main-icons2/placeholder
                       :count      4
                       :icon-color icon-color}]]))

(defn community-tags [tags]
  [react/view (styles/community-tags-container)
   (for [{:keys [id tag-label resource]} tags]
     ^{:key id}
     [react/view {:margin-right 8}
      [tag/tag
       {:id          id
        :size        24
        :label       tag-label
        :type        :emoji
        :labelled    true
        :resource    resource}]])])

(defn community-title [{:keys [title description size] :or {size :small}}]
  [react/view (styles/community-title-description-container (if (= size :large) 56 32))
   (when title
     [text/text
      {:accessibility-label :chat-name-text
       :number-of-lines     1
       :ellipsize-mode      :tail
       :weight              :semi-bold
       :size               (if (= size :large) :heading-1 :heading-2)}
      title])
   (when description
     [text/text
      {:accessibility-label :community-description-text
       :number-of-lines     2
       :ellipsize-mode      :tail
       :weight  :regular
       :size    :paragraph-1
       :style {:margin-top (if (= size :large) 8 2)}}
      description])])

(defn permission-tag-container [{:keys [locked tokens]}]
  [permission/tag {:background-color (colors/theme-colors
                                      colors/neutral-10
                                      colors/neutral-80)
                   :locked           locked
                   :tokens           tokens
                   :size             24}])