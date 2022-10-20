(ns status-im.ui.screens.communities.community-options-bottom-sheet
  (:require  [status-im.i18n.i18n :as i18n]
             [quo.react-native :as rn]             [quo2.components.markdown.text :as text]
             [quo2.components.buttons.button :as button]
             [quo2.components.drawers.action-drawers :as action-drawers]
             [quo2.components.tags.context-tags :as context-tags]
             [status-im.react-native.resources :as resources]
             [status-im.utils.handlers :refer [<sub >evt]]
             [status-im.communities.core :as communities]))

(def not-joined-options [{:icon  :main-icons2/members
                          :label (i18n/label :t/view-members)}
                         {:icon :main-icons2/bullet-list
                          :right-icon :main-icons2/chevron-right
                          :label (i18n/label :t/view-community-rules)}
                         {:icon :main-icons2/add-user
                          :label  (i18n/label :t/invite-contacts)}
                         {:icon :main-icons2/qr-code
                          :label (i18n/label :t/show-qr)}
                         {:icon :main-icons2/share
                          :label (i18n/label :t/share-community)}])

(def joined-options [{:icon :main-icons2/members
                      :label (i18n/label :t/view-members)}
                     {:icon :main-icons2/bullet-list
                      :right-icon :main-icons2/chevron-right
                      :label (i18n/label :t/view-community-rules)}
                     {:icon :main-icons2/up-to-date
                      :label  (i18n/label :t/mark-as-read)}
                     {:icon :main-icons2/muted
                      :label  (i18n/label :t/mute-community)
                      :right-icon :main-icons2/chevron-right}
                     {:icon :main-icons2/notifications
                      :label  (i18n/label :t/community-notification-settings)
                      :right-icon :main-icons2/chevron-right}
                     {:icon :main-icons2/add-user
                      :label  (i18n/label :t/invite-contacts)}
                     {:icon :main-icons2/qr-code
                      :label (i18n/label :t/show-qr)}
                     {:icon :main-icons2/share
                      :label (i18n/label :t/share-community)}])

(defn leave-sheet [community]
  [rn/view {:style {:flex 1 :margin-left 20 :margin-right 20 :margin-bottom 20}}
   [rn/view {:style {:flex 1 :flex-direction :row :align-items :center :justify-content :space-between}}

    [text/text {:accessibility-label :communities-join-community
                :weight              :semi-bold
                :size                :heading-1}
     (i18n/label :t/leave-community?)]]
       ;; TODO get tag image from community data 
   [context-tags/context-tag
    {:style
     {:margin-right :auto
      :margin-top 8}}
    (resources/get-image :status-logo) (:name community)]
   [text/text {:accessibility-label :communities-join-community
               :size                :paragraph-1
               :style {:margin-top 16}}
    (i18n/label :t/leave-community-message)]
   [rn/view {:style {:width "100%"
                     :margin-top 16 :margin-bottom 16
                     :flex 1
                     :flex-direction :row
                     :align-items :center
                     :justify-content :space-evenly}}
    [button/button {:on-press #(>evt [:bottom-sheet/hide])
                    :type :grey :style {:flex 1 :margin-right 12}}   (i18n/label :t/cancel)]
    [button/button
     {:on-press (fn []
                  (>evt [::communities/leave (:id community)])
                  (>evt [:bottom-sheet/hide]))
      :style {:flex 1}}   (i18n/label :t/leave-community)]]])

(defn options-menu []
  (let [community-mock (<sub [:get-screen-params :community-overview]) ;;TODO stop using mock data and only pass community id 
        community (<sub [:communities/community (:id community-mock)])]
    [action-drawers/action-drawer {:actions (if (:joined community)
                                              joined-options
                                              not-joined-options)
                                   :actions-with-consequence
                                   (when (:joined community)
                                     [{:icon :main-icons2/log-out
                                       :label  (i18n/label :t/leave-community)
                                       :on-press #(>evt [:bottom-sheet/show-sheet
                                                         {:content (constantly [leave-sheet community])
                                                          :content-height 300}])}])}]))


