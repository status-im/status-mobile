(ns quo2.components.community.community-list-view
  (:require [quo2.components.community.community-view :as community-view]
            [quo2.components.community.style :as style]
            [quo2.components.counter.counter :as counter]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [react-native.fast-image :as fast-image]))

(defn community-icon-view
  [community-icon]
  [rn/view
   {:width  32
    :height 32}
   [fast-image/fast-image
    {:source {:uri community-icon}
     :style  {:height        32
              :border-radius 16
              :width         32}}]])

(defn notification-view
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
      :color           (colors/theme-colors
                        colors/neutral-40
                        colors/neutral-50)}]
    (pos? unread-mentions-count)
    [counter/counter {:type :default} unread-mentions-count]

    unread-messages?
    [rn/view
     {:style {:width            8
              :height           8
              :border-radius    4
              :background-color (colors/theme-colors
                                 colors/neutral-40
                                 colors/neutral-60)}}]))

(defn communities-list-view-item
  [props
   {:keys [name
           locked?
           status
           muted?
           unread-messages?
           unread-mentions-count
           community-icon
           tokens
           background-color]}]
  [rn/view
   {:style (merge (style/community-card 16)
                  {:margin-bottom     12
                   :margin-horizontal 20})}
   [rn/touchable-highlight
    (merge {:style {:height        56
                    :border-radius 16}}
           props)
    [rn/view {:flex 1}
     [rn/view
      {:flex-direction     :row
       :border-radius      16
       :padding-horizontal 12
       :align-items        :center
       :padding-vertical   8
       :background-color   background-color}
      [rn/view]
      (when community-icon
        [community-icon-view community-icon])
      [rn/view
       {:flex              1
        :margin-horizontal 12}
       [text/text
        {:weight              :semi-bold
         :size                :paragraph-1
         :accessibility-label :community-name-text
         :number-of-lines     1
         :ellipsize-mode      :tail
         :style               {:color (when muted?
                                        (colors/theme-colors
                                         colors/neutral-40
                                         colors/neutral-60))}}
        name]
       [community-view/community-stats-column :list-view]]
      (if (= status :gated)
        [community-view/permission-tag-container
         {:locked? locked?
          :tokens  tokens}]
        [notification-view
         {:muted?                muted?
          :unread-mentions-count unread-mentions-count
          :unread-messages?      unread-messages?}])]]]])

(defn communities-membership-list-item
  [props
   {:keys [name
           muted?
           unread-messages?
           unread-mentions-count
           status
           community-icon
           tokens
           locked?]}]
  [rn/view {:margin-bottom 20}
   [rn/touchable-highlight
    (merge {:underlay-color colors/primary-50-opa-5
            :style          {:border-radius 12}}
           props)
    [rn/view {:flex 1}
     [rn/view
      {:flex-direction :row
       :border-radius  16
       :align-items    :center
       :height         48}

      (when community-icon
        [community-icon-view community-icon])
      [rn/view
       {:flex            1
        :margin-left     12
        :justify-content :center}
       [text/text
        {:accessibility-label :chat-name-text
         :number-of-lines     1
         :ellipsize-mode      :tail
         :weight              :semi-bold
         :size                :paragraph-1}
        name]]

      [rn/view
       {:justify-content :center
        :margin-right    16}
       (if (= status :gated)
         [community-view/permission-tag-container
          {:locked? locked?
           :tokens  tokens}]
         [notification-view
          {:muted?                muted?
           :unread-mentions-count unread-mentions-count
           :unread-messages?      unread-messages?}])]]]]])
