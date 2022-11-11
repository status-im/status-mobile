(ns quo2.components.community.community-list-view
  (:require
   [quo2.components.community.community-view :as community-view]
   [quo2.components.markdown.text :as text]
   [quo2.foundations.colors :as colors]
   [quo2.components.counter.counter :as counter]
   [quo2.components.icon :as icons]
   [quo2.components.community.style :as style]
   [react-native.core :as rn]))

(defn communities-list-view-item [props {:keys [name locked? status notifications
                                                tokens background-color]}]
  [rn/view {:style (merge (style/community-card 16)
                          {:margin-bottom     12
                           :margin-horizontal 20})}
   [rn/touchable-highlight (merge {:style {:height        56
                                           :border-radius 16}}
                                  props)
    [rn/view {:flex 1}
     [rn/view {:flex-direction     :row
               :border-radius      16
               :padding-horizontal 12
               :align-items        :center
               :padding-vertical   8
               :background-color   background-color}
      [rn/view]
       ;;TODO new pure component based on quo2 should be implemented without status-im usage
       ;[communities.icon/community-icon-redesign community 32]]
      [rn/view {:flex              1
                :margin-horizontal 12}
       [text/text {:weight              :semi-bold
                   :size                :paragraph-1
                   :accessibility-label :community-name-text
                   :number-of-lines     1
                   :ellipsize-mode      :tail
                   :style               {:color (when (= notifications :muted)
                                                  (colors/theme-colors
                                                   colors/neutral-40
                                                   colors/neutral-60))}}
        name]
       [community-view/community-stats-column :list-view]]
      (if (= status :gated)
        [community-view/permission-tag-container {:locked? locked?
                                                  :tokens  tokens}]
        (cond
          (= notifications :unread-messages-count)
          [rn/view {:style {:width            8
                            :height           8
                            :border-radius    4
                            :background-color (colors/theme-colors
                                               colors/neutral-40
                                               colors/neutral-60)}}]

          (= notifications :unread-mentions-count)
          [counter/counter {:type :default} 5]

          (= notifications :muted)
          [icons/icon :i/muted {:container-style {:align-items     :center
                                                  :justify-content :center}
                                :resize-mode     :center
                                :size            20
                                :color           (colors/theme-colors
                                                  colors/neutral-40
                                                  colors/neutral-50)}]))]]]])

(defn communities-membership-list-item [props {:keys [name status tokens locked?]}]
  [rn/view {:margin-bottom 20}
   [rn/touchable-highlight (merge {:underlay-color colors/primary-50-opa-5
                                   :style          {:border-radius 12}}
                                  props)
    [rn/view {:flex 1}
     [rn/view {:flex-direction :row
               :border-radius  16
               :align-items    :center}
      ;;TODO new pure component based on quo2 should be implemented without status-im usage
      ;[communities.icon/community-icon-redesign community 32]
      [rn/view {:flex            1
                :margin-left     12
                :justify-content :center}
       [text/text
        {:accessibility-label :chat-name-text
         :number-of-lines     1
         :ellipsize-mode      :tail
         :weight              :semi-bold
         :size                :paragraph-1}
        name]]
      (when (= status :gated)
        [rn/view {:justify-content :center
                  :margin-right    12}
         [community-view/permission-tag-container {:locked? locked?
                                                   :tokens  tokens}]])]]]])
