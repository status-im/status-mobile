(ns quo2.components.community.community-list-view
  (:require [quo2.components.community.community-view :as community-view]
            [quo2.components.community.style :as style]
            [quo2.components.counter.counter :as counter]
            [quo2.components.icon :as icons]
            [quo2.components.markdown.text :as text]
            [quo2.foundations.colors :as colors]
            [quo2.components.community.icon :as community-icon]
            [quo2.components.common.unread-grey-dot.view :refer [unread-grey-dot]]
            [react-native.core :as rn]))

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
    [unread-grey-dot :unviewed-messages-public]))

(defn communities-membership-list-item
  [props
   {:keys [name
           muted?
           unviewed-messages-count
           unviewed-mentions-count
           status
           images
           tokens
           locked?
           style]}]
  [rn/touchable-highlight
   (merge {:underlay-color (colors/theme-colors
                            colors/neutral-5
                            colors/neutral-95)
           :style          {:border-radius 12
                            :margin-left   12}}
          props)
   [rn/view (merge (style/membership-info-container) style)
    [community-icon/community-icon
     {:images images} 32]
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
         :unread-mentions-count unviewed-mentions-count
         :unread-messages?      (pos? unviewed-messages-count)}])]]])
