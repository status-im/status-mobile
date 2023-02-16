(ns status-im2.contexts.communities.menus.community-options.view
  (:require [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [quo2.core :as quo]
            [status-im2.contexts.communities.menus.see-rules.view :as see-rules]
            [status-im2.contexts.communities.menus.leave.view :as leave-menu]))

(defn hide-sheet-and-dispatch
  [event]
  (rf/dispatch [:bottom-sheet/hide])
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
   :on-press            #(rf/dispatch [:bottom-sheet/show-sheet
                                       {:content        (constantly [see-rules/view id])
                                        :content-height 400}])
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
  [id muted?]
  {:icon                (if muted? :i/muted :i/activity-center)
   :accessibility-label (if muted? :unmute-community :mute-community)
   :label               (i18n/label (if muted? :t/unmute-community :t/mute-community))
   :sub-label           (when muted? (str "muted for 15 minutes"))
   :right-icon          :i/chevron-right
   :on-press            #(hide-sheet-and-dispatch [:community/set-muted id (not muted?)])})

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
  [id request-id]
  {:icon                :i/block
   :label               (i18n/label :t/cancel-request-to-join)
   :accessibility-label :cancel-request-to-join
   :danger?             true
   :on-press            #(rf/dispatch [:bottom-sheet/show-sheet
                                       {:content        (constantly [leave-menu/cancel-request-sheet id
                                                                     request-id])
                                        :content-height 400}])})

(defn edit-community
  [id]
  {:icon                :i/edit
   :label               (i18n/label :t/edit-community)
   :accessibility-label :edit-community
   :on-press            #(rf/dispatch [:communities/open-edit-community id]
                                      (rf/dispatch [:bottom-sheet/hide]))})

(defn not-joined-options
  [id token-gated?]
  [[(when-not token-gated? (view-members id))
    (when-not token-gated? (view-rules id))
    (invite-contacts id)
    (when token-gated? (view-token-gating id))
    (show-qr id)
    (share-community id)]])

(defn join-request-sent-options
  [id token-gated? request-id]
  [(conj (first (not-joined-options id token-gated?))
         (assoc (cancel-request-to-join id request-id) :add-divider? true))])

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
  (let [{:keys [token-gated? admin joined
                muted banList]} (rf/sub [:communities/community id])
        request-id              (rf/sub [:communities/my-pending-request-to-join id])]
    (cond
      admin      (owner-options id token-gated? muted)
      joined     (joined-options id token-gated? muted)
      request-id (join-request-sent-options id token-gated? request-id)
      banList    (banned-options id token-gated?)
      :else      (not-joined-options id token-gated?))))

(defn community-options-bottom-sheet
  [id]
  [quo/action-drawer
   (get-context-drawers {:id id})])
