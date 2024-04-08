(ns status-im.contexts.chat.messenger.messages.contact-requests.bottom-drawer.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.constants :as constants]
    [status-im.contexts.chat.messenger.messages.contact-requests.bottom-drawer.style :as style]
    [status-im.contexts.shell.jump-to.constants :as jump-to.constants]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [contact-id]}]
  (let [customization-color             (rf/sub [:profile/customization-color])
        [primary-name _]                (rf/sub [:contacts/contact-two-names-by-identity contact-id])
        {:keys [contact-request-state]} (rf/sub [:chats/current-chat-chat-view])
        chat-type                       (rf/sub [:chats/chat-type])
        contact-request-send?           (or (not contact-request-state)
                                            (= contact-request-state
                                               constants/contact-request-state-none))
        contact-request-received?       (= contact-request-state
                                           constants/contact-request-state-received)
        contact-request-pending?        (= contact-request-state
                                           constants/contact-request-state-sent)]

    [rn/view {:style style/container}
     [quo/permission-context
      {:on-press     #(rf/dispatch [:chat.ui/show-profile contact-id])
       :type         :action
       :action-icon  (cond
                       (= chat-type :community-chat) :i/communities
                       contact-request-pending?      :i/pending-state
                       :else                         :i/add-user)
       :action-label (cond
                       (= chat-type :community-chat)
                       (i18n/label :t/join-community-to-post)

                       (= chat-type :group-chat)
                       (i18n/label :t/group-chat-not-member)

                       contact-request-send?
                       (i18n/label :t/contact-request-chat-add {:name primary-name})

                       contact-request-received?
                       (i18n/label :t/contact-request-chat-received {:name primary-name})

                       contact-request-pending?
                       (i18n/label :t/contact-request-chat-pending))}]
     [quo/floating-shell-button
      {:jump-to
       {:on-press            #(rf/dispatch [:shell/navigate-to-jump-to])
        :customization-color customization-color
        :label               (i18n/label :t/jump-to)}}
      {:position :absolute
       :top      (- jump-to.constants/floating-shell-button-height)}]]))
