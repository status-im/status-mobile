(ns status-im2.contexts.communities.home.actions.view
  (:require [i18n.i18n :as i18n]
            [quo2.core :as quo]
            [react-native.core :as rn]
            [utils.re-frame :as rf]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:bottom-sheet/hide])
  (rf/dispatch event))

(def not-joined-options
  {:actions [{:icon  :i/members
              :label (i18n/label :t/view-members)}
             {:icon       :i/bullet-list
              :right-icon :i/chevron-right
              :label      (i18n/label :t/view-community-rules)}
             {:icon  :i/add-user
              :label (i18n/label :t/invite-contacts)}
             {:icon  :i/qr-code
              :label (i18n/label :t/show-qr)}
             {:icon  :i/share
              :label (i18n/label :t/share-community)}]})

(defn joined-options
  [id]
  {:actions
   [{:icon                :i/members
     :accessibility-label :i/view-members
     :label               (i18n/label :t/view-members)
     :on-press            #(hide-sheet-and-dispatch [:navigate-to :community-members
                                                     {:community-id id}])}
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

(defn leave-sheet
  [community]
  [rn/view {:style {:flex 1 :margin-left 20 :margin-right 20 :margin-bottom 20}}
   [rn/view {:style {:flex 1 :flex-direction :row :align-items :center :justify-content :space-between}}

    [quo/text
     {:accessibility-label :communities-join-community
      :weight              :semi-bold
      :size                :heading-1}
     (i18n/label :t/leave-community?)]]
   ;; TODO get tag image from community data
   #_[quo/context-tag
      {:style
       {:margin-right :auto
        :margin-top   8}}
      (resources/get-image :status-logo) (:name community)]
   [quo/text
    {:accessibility-label :communities-join-community
     :size                :paragraph-1
     :style               {:margin-top 16}}
    (i18n/label :t/leave-community-message)]
   [rn/view
    {:style {:margin-top      16
             :margin-bottom   16
             :flex            1
             :flex-direction  :row
             :align-items     :center
             :justify-content :space-evenly}}
    [quo/button
     {:on-press #(rf/dispatch [:bottom-sheet/hide])
      :type     :grey
      :style    {:flex         1
                 :margin-right 12}}
     (i18n/label :t/cancel)]
    [quo/button
     {:on-press #(hide-sheet-and-dispatch [:communities/leave (:id community)])
      :style    {:flex 1}}
     (i18n/label :t/leave-community)]]])

(defn actions
  [id]
  (let [community (rf/sub [:communities/community id])]
    [quo/action-drawer
     [(get (if (:joined community)
             (joined-options (:id community))
             not-joined-options)
           :actions)
      (when (:joined community)
        [{:icon     :i/log-out
          :label    (i18n/label :t/leave-community)
          :on-press #(rf/dispatch [:bottom-sheet/show-sheet
                                   {:content        (constantly [leave-sheet community])
                                    :content-height 300}])}])]]))
