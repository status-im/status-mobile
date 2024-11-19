(ns status-im.contexts.chat.messenger.messages.contact-requests.bottom-drawer.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.constants :as constants]
    [status-im.contexts.chat.messenger.messages.contact-requests.bottom-drawer.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [contact-id]}]
  (let [{:keys [contact-request-state
                community-id
                chat-name]}       (rf/sub [:chats/current-chat-chat-view])
        chat-type                 (rf/sub [:chats/chat-type])
        [primary-name _]          (if (= chat-type :public-chat)
                                    [(str "#" chat-name) ""]
                                    (rf/sub [:contacts/contact-two-names-by-identity contact-id]))
        community-chat?           (= chat-type :community-chat)
        joined                    (when community-chat?
                                    (rf/sub [:communities/community-joined community-id]))
        pending?                  (when community-chat?
                                    (rf/sub [:communities/my-pending-request-to-join community-id]))
        contact-request-send?     (or (not contact-request-state)
                                      (= contact-request-state
                                         constants/contact-request-state-none))
        contact-request-received? (= contact-request-state
                                     constants/contact-request-state-received)
        contact-request-pending?  (= contact-request-state
                                     constants/contact-request-state-sent)]
    [rn/view {:style style/container}
     [quo/permission-context
      {:blur?        true
       :on-press     (cond
                       (and community-chat? (not pending?) (not joined))
                       #(rf/dispatch [:open-modal :community-account-selection-sheet
                                      {:community-id community-id}])

                       (not community-chat?)
                       #(rf/dispatch [:chat.ui/show-profile contact-id]))
       :type         :action
       :action-icon  (cond
                       community-chat?          :i/communities
                       contact-request-pending? :i/pending-state
                       :else                    :i/add-user)
       :action-label (cond
                       community-chat?
                       (cond
                         pending?
                         (i18n/label :t/request-to-join-community-pending)

                         joined
                         (i18n/label :t/no-permissions-to-post)

                         :else
                         (i18n/label :t/join-community-to-post))

                       (= chat-type :group-chat)
                       (i18n/label :t/group-chat-not-member)

                       contact-request-send?
                       (i18n/label :t/contact-request-chat-add {:name primary-name})

                       contact-request-received?
                       (i18n/label :t/contact-request-chat-received {:name primary-name})

                       contact-request-pending?
                       (i18n/label :t/contact-request-chat-pending))}]]))
