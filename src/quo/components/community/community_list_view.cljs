(ns quo.components.community.community-list-view
  (:require
    [quo.components.common.unread-grey-dot.view :refer [unread-grey-dot]]
    [quo.components.community.community-view :as community-view]
    [quo.components.community.icon :as community-icon]
    [quo.components.community.style :as style]
    [quo.components.counter.counter.view :as counter]
    [quo.components.icon :as icons]
    [quo.components.markdown.text :as text]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]))

(defn notification-view
  [{:keys [theme
           muted?
           unread-messages?
           unread-mentions-count
           customization-color]}]
  [rn/view {:style style/notification-container}
   (cond
     muted?
     [icons/icon :i/muted
      {:container-style {:align-items     :center
                         :justify-content :center}
       :resize-mode     :center
       :size            20
       :color           (colors/theme-colors
                         colors/neutral-50
                         colors/neutral-40
                         theme)}]
     (pos? unread-mentions-count)
     [counter/view
      {:customization-color customization-color
       :type                :default}
      unread-mentions-count]

     unread-messages?
     [unread-grey-dot :unviewed-messages-public])])

(defn- title-color
  [unread-messages? muted theme]
  (cond
    muted            (colors/theme-colors colors/neutral-40 colors/neutral-60 theme)
    unread-messages? (colors/theme-colors colors/neutral-100 colors/white theme)
    :else            (colors/theme-colors colors/neutral-50 colors/neutral-40 theme)))

(defn communities-membership-list-item
  [{:keys [customization-color] :as props}
   bottom-sheet?
   {:keys [name muted unviewed-messages-count unviewed-mentions-count status
           images tokens locked? style]}]
  (let [theme            (quo.theme/use-theme)
        unread-messages? (pos? unviewed-messages-count)]
    [rn/touchable-highlight
     (merge {:underlay-color (colors/theme-colors
                              colors/neutral-5
                              colors/neutral-95
                              theme)
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
         :size                :paragraph-1
         :style               {:color (title-color unread-messages? muted theme)}}
        name]]

      [rn/view
       {:justify-content :center
        :margin-right    (when bottom-sheet?
                           16)}
       (if (= status :gated)
         [community-view/permission-tag-container
          {:locked? locked?
           :tokens  tokens}]
         [notification-view
          {:theme                 theme
           :customization-color   customization-color
           :muted?                muted
           :unread-mentions-count unviewed-mentions-count
           :unread-messages?      unread-messages?}])]]]))
