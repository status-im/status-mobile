(ns status-im2.contexts.communities.menus.community-options.view
  (:require [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [quo2.core :as quo]
            [status-im2.contexts.communities.menus.leave.view :as leave-menu]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch-sync [:dismiss-bottom-sheet])
  (rf/dispatch event))

(defn view-members
  [id]
  {:icon                :i/members
   :accessibility-label :view-members
   :label               (i18n/label :t/view-members)
   :on-press            #(hide-sheet-and-dispatch [:navigate-to :community-members
                                                   {:community-id id}])})

(defn view-rules
  [id]
  {:icon                :i/bullet-list
   :right-icon          :i/chevron-right
   :accessibility-label :view-community-rules
   :on-press            #(js/alert (str "implement action" id))
   :label               (i18n/label :t/view-community-rules)})

(defn view-token-gating
  [id]
  {:icon                :i/bullet-list
   :right-icon          :i/token
   :accessibility-label :view-token-gating
   :on-press            #(js/alert (str "implement action" id))
   :label               (i18n/label :t/view-token-gating)})

(defn mark-as-read
  [id]
  {:icon                :i/up-to-date
   :accessibility-label :mark-as-read
   :label               (i18n/label :t/mark-as-read)
   :on-press            #(hide-sheet-and-dispatch [:chat.ui/mark-all-read-in-community-pressed id])})

(defn mute-community
  [_ muted?]
  {:icon                (if muted? :i/muted :i/activity-center)
   :accessibility-label (if muted? :unmute-community :mute-community)
   :label               (i18n/label (if muted? :t/unmute-community :t/mute-community))
   :sub-label           (when muted? (str "muted for 15 minutes"))
   :right-icon          :i/chevron-right})

(defn community-notification-settings
  [id]
  {:icon                :i/notifications
   :accessibility-label :community-notification-settings
   :label               (i18n/label :t/community-notification-settings)
   :on-press            #(js/alert (str "implement action" id))
   :right-icon          :i/chevron-right})

(defn invite-contacts
  [id]
  {:icon                :i/add-user
   :accessibility-label :invite-people-from-contacts
   :label               (i18n/label :t/invite-people-from-contacts)
   :on-press            #(hide-sheet-and-dispatch [:communities/invite-people-pressed id])})

(defn show-qr
  [id]
  {:icon                :i/qr-code
   :accessibility-label :show-qr
   :on-press            #(js/alert (str "implement action" id))
   :label               (i18n/label :t/show-qr)})

(defn share-community
  [id]
  {:icon                :i/share
   :accessibility-label :share-community
   :label               (i18n/label :t/share-community)
   :on-press            #(hide-sheet-and-dispatch [:communities/share-community-pressed id])})

(defn leave-community
  [id]
  {:icon                :i/log-out
   :label               (i18n/label :t/leave-community)
   :accessibility-label :leave-community

   :danger?             true
   :on-press            #(rf/dispatch [:bottom-sheet/show-sheet
                                       {:content        (constantly [leave-menu/leave-sheet id])
                                        :content-height 400}])})

(defn cancel-request-to-join
  [id]
  {:icon                :i/block
   :label               (i18n/label :t/cancel-request-to-join)
   :accessibility-label :cancel-request-to-join
   :danger?             true
   :on-press            #(js/alert (str "implement action" id))})

(defn edit-community
  [id]
  {:icon                :i/edit
   :label               (i18n/label :t/edit-community)
   :accessibility-label :edit-community
   :on-press            #(js/alert (str "implement action" id))})

(defn not-joined-options
  [id token-gated?]
  [[(when-not token-gated? (view-members id))
    (when-not token-gated? (view-rules id))
    (invite-contacts id)
    (when token-gated? (view-token-gating id))
    (show-qr id)
    (share-community id)]])

(defn join-request-sent-options
  [id token-gated?]
  [(conj (first (not-joined-options id token-gated?))
         (assoc (cancel-request-to-join id) :add-divider? true))])

(defn banned-options
  [id token-gated?]
  (not-joined-options id token-gated?))

(defn joined-options
  [id token-gated? muted?]
  [[(view-members id)
    (view-rules id)
    (when token-gated? (view-token-gating id))
    (mark-as-read id)
    (mute-community id muted?)
    (community-notification-settings id)
    (invite-contacts id)
    (show-qr id)
    (share-community id)]
   [(assoc (leave-community id) :add-divider? true)]])

(defn owner-options
  [id token-gated? muted?]
  [[(view-members id)
    (view-rules id)
    (when token-gated? (view-token-gating id))
    (edit-community id)
    (mark-as-read id)
    (mute-community id muted?)
    (community-notification-settings id)
    (invite-contacts id)
    (show-qr id)
    (share-community id)]])

(defn get-context-drawers
  [{:keys [id]}]
  (let [community     (rf/sub [:communities/community id])
        token-gated?  (:token-gated? community)
        joined?       (:joined community)
        admin?        (:admin community)
        request-sent? (pos? (:requested-to-join-at community))
        muted?        (:muted community)
        banned?       (:banList community)]
    (cond
      joined?       (joined-options id token-gated? muted?)
      admin?        (owner-options id token-gated? muted?)
      request-sent? (join-request-sent-options id token-gated?)
      banned?       (banned-options id token-gated?)
      :else         (not-joined-options id token-gated?))))

(defn community-options-bottom-sheet
  [id]
  [quo/action-drawer
   (get-context-drawers {:id id})])
