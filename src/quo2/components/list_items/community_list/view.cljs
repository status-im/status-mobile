(ns quo2.components.list-items.community-list.view
  (:require [quo2.components.community.icon :as community-icon]
            [quo2.components.community.community-view :as community-view]
            [quo2.components.list-items.community-list.style :as style]
            [quo2.foundations.colors :as colors]
            [quo2.components.icon :as icons]
            [quo2.components.counter.counter :as counter]
            [quo2.components.common.unread-grey-dot.view :refer [unread-grey-dot]]
            [quo2.components.markdown.text :as text]
            [react-native.core :as rn]))

(defn- notification-view
  [{:keys [muted?
           unread-messages?
           unread-mentions-count]}]
  (cond
    muted?
    [icons/icon :i/muted
     {:container-style {:align-items     :center
                        :justify-content :center}
      :resize-mode     :center
      :size            20
      :color           (colors/theme-colors colors/neutral-40 colors/neutral-50)}]
    (pos? unread-mentions-count)
    [counter/counter {:type :default} unread-mentions-count]

    unread-messages?
    [unread-grey-dot :unviewed-messages-public]))

(defn view
  [props
   {:keys [name
           locked?
           status
           muted?
           unread-messages?
           unread-mentions-count
           community-icon
           tokens]}]
  [rn/view
   {:style (merge (style/community-card 16)
                  {:margin-bottom 12})}
   [rn/touchable-highlight
    (merge {:style {:height        56
                    :border-radius 16}}
           props)
    [rn/view {:style style/detail-container}
     [rn/view (style/list-info-container)
      [community-icon/community-icon
       {:images community-icon} 32]
      [rn/view
       {:style {:flex              1
                :margin-horizontal 12}}
       [text/text
        {:weight              :semi-bold
         :size                :paragraph-1
         :accessibility-label :community-name-text
         :number-of-lines     1
         :ellipsize-mode      :tail
         :style               {:color (when muted?
                                        (colors/theme-colors colors/neutral-40 colors/neutral-60))}}
        name]
       [community-view/community-stats-column
        {:type :list-view}]]
      (if (= status :gated)
        [community-view/permission-tag-container
         {:locked? locked?
          :tokens  tokens}]
        [notification-view
         {:muted?                muted?
          :unread-mentions-count unread-mentions-count
          :unread-messages?      unread-messages?}])]]]])
