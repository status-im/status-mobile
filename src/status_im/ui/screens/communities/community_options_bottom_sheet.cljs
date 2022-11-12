(ns status-im.ui.screens.communities.community-options-bottom-sheet
  (:require  [status-im.i18n.i18n :as i18n]
             [quo.react-native :as rn]
             [quo2.components.markdown.text :as text]
             [quo2.components.buttons.button :as button]
             [quo2.components.drawers.action-drawers :as action-drawers]
             [quo2.components.tags.context-tags :as context-tags]
             [status-im.react-native.resources :as resources]
             [utils.re-frame :as rf]
             [status-im.communities.core :as communities]))

(defn hide-sheet-and-dispatch [event]
  (rf/dispatch [:bottom-sheet/hide])
  (rf/dispatch event))

(def not-joined-options
  {:actions [{:icon       :i/members
              :label      (i18n/label :t/view-members)}
             {:icon       :i/bullet-list
              :right-icon :i/chevron-right
              :label      (i18n/label :t/view-community-rules)}
             {:icon       :i/add-user
              :label      (i18n/label :t/invite-contacts)}
             {:icon       :i/qr-code
              :label      (i18n/label :t/show-qr)}
             {:icon       :i/share
              :label      (i18n/label :t/share-community)}]})

(defn joined-options [id]
  {:actions [{:icon                :i/members
              :accessibility-label :i/view-members
              :label               (i18n/label :t/view-members)
              :on-press            #(hide-sheet-and-dispatch [:navigate-to :community-members {:community-id id}])}
             {:icon                :i/bullet-list
              :right-icon          :i/chevron-right
              :accessibility-label :view-community-rules
              :label               (i18n/label :t/view-community-rules)}
             {:icon                :i/up-to-date
              :accessibility-label :mark-as-read
              :label               (i18n/label :t/mark-as-read)
              :on-press            #(hide-sheet-and-dispatch [:chat.ui/mark-all-read-in-community-pressed id])}
             {:icon                :i/muted
              :accessibility-label :mute-community
              :label               (i18n/label :t/mute-community)
              :right-icon          :i/chevron-right}
             {:icon                :i/notifications
              :accessibility-label :community-notification-settings
              :label               (i18n/label :t/community-notification-settings)
              :right-icon          :i/chevron-right}
             {:icon                :i/add-user
              :accessibility-label :invite-people-from-contacts
              :label               (i18n/label :t/invite-people-from-contacts)
              :on-press            #(hide-sheet-and-dispatch [:communities/invite-people-pressed id])}
             {:icon                :i/qr-code
              :accessibility-label :show-qr
              :label               (i18n/label :t/show-qr)}
             {:icon                :i/share
              :accessibility-label :share-community
              :label               (i18n/label :t/share-community)
              :on-press            #(hide-sheet-and-dispatch [:communities/share-community-pressed id])}]})

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
      :margin-top   8}}
    (resources/get-image :status-logo) (:name community)]
   [text/text {:accessibility-label :communities-join-community
               :size                :paragraph-1
               :style               {:margin-top 16}}
    (i18n/label :t/leave-community-message)]
   [rn/view {:style {:width           "100%"
                     :margin-top      16
                     :margin-bottom   16
                     :flex            1
                     :flex-direction  :row
                     :align-items     :center
                     :justify-content :space-evenly}}
    [button/button {:on-press #(rf/dispatch [:bottom-sheet/hide])
                    :type     :grey
                    :style    {:flex         1
                               :margin-right 12}}
     (i18n/label :t/cancel)]
    [button/button {:on-press (fn []
                                #(rf/dispatch [::communities/leave (:id community)])
                                #(rf/dispatch [:bottom-sheet/hide]))
                    :style {:flex 1}}
     (i18n/label :t/leave-community)]]])

(defn options-menu []
  (let [community-mock (rf/sub [:get-screen-params :community-overview]) ;;TODO stop using mock data and only pass community id
        community (rf/sub [:communities/community (:id community-mock)])]
    [action-drawers/action-drawer [(get (if (:joined community)
                                          (joined-options (:id community))
                                          not-joined-options) :actions)
                                   (when (:joined community)
                                     [{:icon      :i/log-out
                                       :label     (i18n/label :t/leave-community)
                                       :on-press  #(rf/dispatch [:bottom-sheet/show-sheet
                                                                 {:content        (constantly [leave-sheet community])
                                                                  :content-height 300}])}])]]))


