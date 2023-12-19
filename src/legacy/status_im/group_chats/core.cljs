(ns legacy.status-im.group-chats.core
  (:refer-clojure :exclude [remove])
  (:require
    [clojure.spec.alpha :as spec]
    [clojure.string :as string]
    [legacy.status-im.data-store.invitations :as data-store.invitations]
    [oops.core :as oops]
    [re-frame.core :as re-frame]
    [status-im2.constants :as constants]
    [status-im2.contexts.chat.events :as chat.events]
    [status-im2.contexts.shell.activity-center.events :as activity-center]
    [status-im2.navigation.events :as navigation]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(rf/defn navigate-chat-updated
  {:events [:navigate-chat-updated]}
  [cofx chat-id]
  (when (get-in cofx [:db :chats chat-id])
    (chat.events/pop-to-root-and-navigate-to-chat cofx chat-id nil)))

(rf/defn handle-chat-removed
  {:events [:chat-removed]}
  [cofx response chat-id]
  (rf/merge cofx
            {:db         (dissoc (:db cofx) :current-chat-id)
             :dispatch-n [[:shell/close-switcher-card chat-id]
                          [:sanitize-messages-and-process-response response]
                          [:pop-to-root :shell-stack]]}
            (activity-center/notifications-fetch-unread-count)))

(rf/defn handle-chat-update
  {:events [:chat-updated]}
  [_ response do-not-navigate?]
  {:dispatch-n [[:sanitize-messages-and-process-response response]
                (when-not do-not-navigate?
                  [:navigate-chat-updated
                   (-> response
                       (oops/oget :chats)
                       first
                       (oops/oget :id))])]})

(rf/defn remove-member
  "Format group update message and sign membership"
  {:events [:group-chats.ui/remove-member-pressed]}
  [_ chat-id member do-not-navigate?]
  {:json-rpc/call [{:method      "wakuext_removeMemberFromGroupChat"
                    :params      [nil chat-id member]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:chat-updated % true])}]})

(rf/defn remove-members
  {:events [:group-chats.ui/remove-members-pressed]}
  [{{:keys [current-chat-id] :group-chat/keys [deselected-members]} :db :as cofx}]
  {:json-rpc/call [{:method      "wakuext_removeMembersFromGroupChat"
                    :params      [nil current-chat-id deselected-members]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:chat-updated % true])
                    :on-error    #()}]})

(rf/defn join-chat
  {:events [:group-chats.ui/join-pressed]}
  [_ chat-id]
  {:json-rpc/call [{:method      "wakuext_confirmJoiningGroup"
                    :params      [chat-id]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:chat-updated %])}]})

(rf/defn create
  {:events       [:group-chats.ui/create-pressed]
   :interceptors [(re-frame/inject-cofx :random-guid-generator)]}
  [{:keys [db] :as cofx} group-name]
  (let [selected-contacts (:group/selected-contacts db)]
    {:json-rpc/call [{:method      "wakuext_createGroupChatWithMembers"
                      :params      [nil group-name (into [] selected-contacts)]
                      :js-response true
                      :on-success  #(re-frame/dispatch [:chat-updated %])}]}))

(rf/defn create-from-link
  {:events [:group-chats/create-from-link]}
  [cofx {:keys [chat-id invitation-admin chat-name]}]
  (if (get-in cofx [:db :chats chat-id])
    {:dispatch [:chat/pop-to-root-and-navigate-to-chat chat-id]}
    {:json-rpc/call [{:method      "wakuext_createGroupChatFromInvitation"
                      :params      [chat-name chat-id invitation-admin]
                      :js-response true
                      :on-success  #(re-frame/dispatch [:chat-updated %])}]}))

(rf/defn make-admin
  {:events [:group-chats.ui/make-admin-pressed]}
  [_ chat-id member]
  {:json-rpc/call [{:method      "wakuext_addAdminsToGroupChat"
                    :params      [nil chat-id [member]]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:chat-updated %])}]})

(rf/defn add-members
  "Add members to a group chat"
  {:events [:group-chats.ui/add-members-pressed]}
  [{{:keys [current-chat-id] :group-chat/keys [selected-participants]} :db :as cofx}]
  {:json-rpc/call [{:method      "wakuext_addMembersToGroupChat"
                    :params      [nil current-chat-id selected-participants]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:chat-updated % true])}]})

(rf/defn add-members-from-invitation
  "Add members to a group chat"
  {:events [:group-chats.ui/add-members-from-invitation]}
  [{{:keys [current-chat-id] :as db} :db :as cofx} id participant]
  {:db            (assoc-in db [:group-chat/invitations id :state] constants/invitation-state-approved)
   :json-rpc/call [{:method      "wakuext_addMembersToGroupChat"
                    :params      [nil current-chat-id [participant]]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:chat-updated %])}]})

(rf/defn leave
  "Leave chat"
  {:events [:group-chats.ui/leave-chat-confirmed]}
  [{:keys [db] :as cofx} chat-id]
  {:json-rpc/call [{:method      "wakuext_leaveGroupChat"
                    :params      [nil chat-id true]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:chat-removed % chat-id])}]})

(rf/defn remove
  "Remove chat"
  {:events [:group-chats.ui/remove-chat-confirmed]}
  [cofx chat-id]
  (rf/merge cofx
            (chat.events/deactivate-chat chat-id)
            (navigation/pop-to-root :shell-stack)))

(def not-blank?
  (complement string/blank?))

(defn- valid-name?
  [name]
  (spec/valid? not-blank? name))

(rf/defn name-changed
  "Save chat from edited profile"
  {:events [:group-chats.ui/name-changed]}
  [{:keys [db] :as cofx} chat-id new-name]
  (when (valid-name? new-name)
    {:db            (assoc-in db [:chats chat-id :name] new-name)
     :json-rpc/call [{:method      "wakuext_changeGroupChatName"
                      :params      [nil chat-id new-name]
                      :js-response true
                      :on-success  #(re-frame/dispatch [:chat-updated %])}]}))

(rf/defn membership-retry
  {:events [:group-chats.ui/membership-retry]}
  [{{:keys [current-chat-id] :as db} :db}]
  {:db (assoc-in db [:chat/memberships current-chat-id :retry?] true)})

(rf/defn membership-message
  {:events [:group-chats.ui/update-membership-message]}
  [{{:keys [current-chat-id] :as db} :db} message]
  {:db (assoc-in db [:chat/memberships current-chat-id :message] message)})

(rf/defn send-group-chat-membership-request
  "Send group chat membership request"
  {:events [:send-group-chat-membership-request]}
  [{{:keys [current-chat-id chats] :as db} :db :as cofx}]
  (let [{:keys [invitation-admin]} (get chats current-chat-id)
        message                    (get-in db [:chat/memberships current-chat-id :message])]
    {:db            (assoc-in db [:chat/memberships current-chat-id] nil)
     :json-rpc/call [{:method      "wakuext_sendGroupChatInvitationRequest"
                      :params      [nil current-chat-id invitation-admin message]
                      :js-response true
                      :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response %])}]}))

(rf/defn send-group-chat-membership-rejection
  "Send group chat membership rejection"
  {:events [:send-group-chat-membership-rejection]}
  [cofx invitation-id]
  {:json-rpc/call [{:method      "wakuext_sendGroupChatInvitationRejection"
                    :params      [nil invitation-id]
                    :js-response true
                    :on-success  #(re-frame/dispatch [:sanitize-messages-and-process-response %])}]})

(rf/defn handle-invitations
  [{db :db} invitations]
  {:db (update db
               :group-chat/invitations
               #(reduce (fn [acc {:keys [id] :as inv}]
                          (assoc acc id inv))
                        %
                        invitations))})

(defn member-removed?
  [{:keys [membership-update-events]} pk]
  (->> membership-update-events
       (filter #(contains? (set (:members %)) pk))
       (sort-by :clockValue >)
       first
       :type
       (= constants/invitation-state-removed)))

(rf/defn deselect-member
  {:events [:deselect-member]}
  [{:keys [db]} id]
  {:db (update db :group-chat/deselected-members conj id)})

(rf/defn undo-deselect-member
  {:events [:undo-deselect-member]}
  [{:keys [db]} id]
  {:db (update db :group-chat/deselected-members disj id)})

(rf/defn deselect-contact
  {:events [:deselect-contact]}
  [{:keys [db]} id]
  {:db (update db :group/selected-contacts disj id)})

(rf/defn select-contact
  {:events [:select-contact]}
  [{:keys [db]} id]
  {:db (update db :group/selected-contacts conj id)})

(rf/defn clear-contacts
  {:events [:group-chat/clear-contacts]}
  [{:keys [db]}]
  {:db (assoc db :group/selected-contacts #{})})

(rf/defn deselect-participant
  {:events [:deselect-participant]}
  [{:keys [db]} id]
  {:db (update db :group-chat/selected-participants disj id)})

(rf/defn select-participant
  {:events [:select-participant]}
  [{:keys [db]} id]
  {:db (update db :group-chat/selected-participants conj id)})

(rf/defn clear-added-participants
  {:events [:group/clear-added-participants]}
  [{db :db}]
  {:db (assoc db :group-chat/selected-participants #{})})

(rf/defn clear-removed-members
  {:events [:group/clear-removed-members]}
  [{db :db}]
  {:db (assoc db :group-chat/deselected-members #{})})

(rf/defn show-group-chat-profile
  {:events [:show-group-chat-profile]}
  [{:keys [db] :as cofx} chat-id]
  (rf/merge cofx
            {:db (-> db
                     (assoc :new-chat-name (get-in db [:chats chat-id :name]))
                     (assoc :current-chat-id chat-id))}
            (navigation/navigate-to :group-chat-profile nil)))

(rf/defn ui-leave-chat-pressed
  {:events [:group-chats.ui/leave-chat-pressed]}
  [{:keys [db]} chat-id]
  (let [chat-name (get-in db [:chats chat-id :name])]
    {:ui/show-confirmation
     {:title               (i18n/label :t/leave-confirmation {:chat-name chat-name})
      :content             (i18n/label :t/leave-chat-confirmation)
      :confirm-button-text (i18n/label :t/leave)
      :on-accept           #(do
                              (re-frame/dispatch [:bottom-sheet/hide-old])
                              (re-frame/dispatch [:group-chats.ui/leave-chat-confirmed chat-id]))}}))

(rf/defn initialize-invitations
  {:events [::initialize-invitations]}
  [{:keys [db]} invitations]
  {:db (assoc db
              :group-chat/invitations
              (reduce (fn [acc {:keys [id] :as inv}]
                        (assoc acc id (data-store.invitations/<-rpc inv)))
                      {}
                      invitations))})

(rf/defn get-group-chat-invitations
  [_]
  {:json-rpc/call
   [{:method     "wakuext_getGroupChatInvitations"
     :on-success #(re-frame/dispatch [::initialize-invitations %])}]})
