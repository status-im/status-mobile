(ns status-im.communities.core
  (:require
   [re-frame.core :as re-frame]
   [clojure.walk :as walk]
   [taoensso.timbre :as log]
   [status-im.utils.fx :as fx]
   [status-im.constants :as constants]
   [status-im.chat.models :as models.chat]
   [status-im.transport.filters.core :as models.filters]
   [status-im.bottom-sheet.core :as bottom-sheet]
   [status-im.data-store.chats :as data-store.chats]
   [status-im.ethereum.json-rpc :as json-rpc]))

(def featured
  [{:name "Status"
    :id constants/status-community-id}])

(def access-no-membership 1)
(def access-invitation-only 2)
(def access-on-request 3)

(defn <-chats-rpc [chats]
  (reduce-kv (fn [acc k v]
               (assoc acc
                      (name k)
                      (-> v
                          (update :members walk/stringify-keys)
                          (assoc :identity {:display-name (get-in v [:identity :display_name])
                                            :description (get-in v [:identity :description])}
                                 :id (name k)))))
             {}
             chats))

(defn <-rpc [{:keys [description] :as c}]
  (let [identity (:identity description)]
    (-> c
        (update-in [:description :members] walk/stringify-keys)
        (assoc-in [:description :identity] {:display-name (:display_name identity)
                                            :description (:description identity)})
        (update-in [:description :chats] <-chats-rpc))))

(fx/defn handle-chats [cofx chats]
  (models.chat/ensure-chats cofx chats))

(fx/defn handle-filters [cofx filters]
  (models.filters/handle-filters cofx filters))

(fx/defn handle-removed-filters [cofx filters]
  (models.filters/handle-filters-removed cofx (map models.filters/responses->filters filters)))

(fx/defn handle-removed-chats [{:keys [db]} chat-ids]
  {:db (reduce (fn [db chat-id]
                 (update db :chats dissoc chat-id))
               db
               chat-ids)})

(fx/defn handle-community
  [{:keys [db]} {:keys [id] :as community}]
  {:db (assoc-in db [:communities id] (<-rpc community))})

(fx/defn handle-fetched
  {:events [::fetched]}
  [{:keys [db]} communities]
  {:db (reduce (fn [db {:keys [id] :as community}]
                 (assoc-in db [:communities id] (<-rpc community)))
               db
               communities)})

(fx/defn handle-response [cofx response]
  (fx/merge cofx
            (handle-removed-chats (:removedChats response))
            (handle-chats (map #(-> %
                                    (data-store.chats/<-rpc)
                                    (dissoc :unviewed-messages-count))
                               (:chats response)))
            (handle-fetched (:communities response))
            (handle-removed-filters (:removedFilters response))
            (handle-filters (:filters response))))

(fx/defn left
  {:events [::left]}
  [cofx response]
  (handle-response cofx response))

(fx/defn joined
  {:events [::joined]}
  [cofx response]
  (handle-response cofx response))

(fx/defn export
  [cofx community-id on-success]
  {::json-rpc/call [{:method "wakuext_exportCommunity"
                     :params [community-id]
                     :on-success on-success
                     :on-error #(do
                                  (log/error "failed to export community" community-id %)
                                  (re-frame/dispatch [::failed-to-export %]))}]})
(fx/defn import-community
  {:events [::import]}
  [cofx community-key on-success]
  {::json-rpc/call [{:method "wakuext_importCommunity"
                     :params [community-key]
                     :on-success on-success
                     :on-error #(do
                                  (log/error "failed to import community" %)
                                  (re-frame/dispatch [::failed-to-import %]))}]})

(fx/defn join
  {:events [::join]}
  [cofx community-id]
  {::json-rpc/call [{:method "wakuext_joinCommunity"
                     :params [community-id]
                     :on-success #(re-frame/dispatch [::joined %])
                     :on-error #(do
                                  (log/error "failed to join community" community-id %)
                                  (re-frame/dispatch [::failed-to-join %]))}]})

(fx/defn leave
  {:events [::leave]}
  [cofx community-id]
  {::json-rpc/call [{:method "wakuext_leaveCommunity"
                     :params [community-id]
                     :on-success #(re-frame/dispatch [::left %])
                     :on-error #(do
                                  (log/error "failed to leave community" community-id %)
                                  (re-frame/dispatch [::failed-to-leave %]))}]})

(fx/defn fetch [_]
  {::json-rpc/call [{:method "wakuext_communities"
                     :params []
                     :on-success #(re-frame/dispatch [::fetched %])
                     :on-error #(do
                                  (log/error "failed to fetch communities" %)
                                  (re-frame/dispatch [::failed-to-fetch %]))}]})

(fx/defn chat-created
  {:events [::chat-created]}
  [cofx community-id user-pk]
  {::json-rpc/call [{:method "wakuext_sendChatMessage"
                     :params [{:chatId user-pk
                               :text "Upgrade here to see an invitation to community"
                               :communityId community-id
                               :contentType constants/content-type-community}]
                     :on-success
                     #(re-frame/dispatch [:transport/message-sent % 1])
                     :on-failure #(log/error "failed to send a message" %)}]})

(fx/defn invite-user [cofx
                      community-id
                      user-pk
                      on-success-event
                      on-failure-event]

  (fx/merge cofx
            {::json-rpc/call [{:method "wakuext_inviteUserToCommunity"
                               :params [community-id
                                        user-pk]
                               :on-success #(re-frame/dispatch [on-success-event %])
                               :on-error #(do
                                            (log/error "failed to invite-user community" %)
                                            (re-frame/dispatch [on-failure-event %]))}]}
            (models.chat/upsert-chat {:chat-id user-pk
                                      :active (get-in cofx [:db :chats user-pk :active])}
                                     #(re-frame/dispatch [::chat-created community-id user-pk]))))

(fx/defn create [{:keys [db]}
                 community-name
                 community-description
                 community-membership
                 on-success-event
                 on-failure-event]
  (let [membership (js/parseInt community-membership)
        my-public-key (get-in db [:multiaccount :public-key])]
    {::json-rpc/call [{:method "wakuext_createCommunity"
                       :params [{:identity {:display_name community-name
                                            :description community-description}
                                 :members {my-public-key {}}
                                 :permissions {:access membership}}]
                       :on-success #(re-frame/dispatch [on-success-event %])
                       :on-error #(do
                                    (log/error "failed to create community" %)
                                    (re-frame/dispatch [on-failure-event %]))}]}))

(defn create-channel [community-id
                      community-channel-name
                      community-channel-description
                      on-success-event
                      on-failure-event]
  {::json-rpc/call [{:method "wakuext_createCommunityChat"
                     :params [community-id
                              {:identity {:display_name community-channel-name
                                          :description community-channel-description}
                               :permissions {:access access-no-membership}}]
                     :on-success #(re-frame/dispatch [on-success-event %])
                     :on-error #(do
                                  (log/error "failed to create community channel" %)
                                  (re-frame/dispatch [on-failure-event %]))}]})

(def no-membership-access 1)
(def invitation-only-access 2)
(def on-request-access 3)

(defn require-membership? [permissions]
  (not= no-membership-access (:access permissions)))

(def community-id-length 68)
;; TODO: test this
(defn can-post? [{:keys [admin] :as community} pk local-chat-id]
  (let [chat-id (subs local-chat-id community-id-length)
        can-access-community? (or (get-in community [:description :members pk])
                                  (not (require-membership? (get-in community [:description :permissions]))))]
    (or admin
        (get-in community [:description :chats chat-id :members pk])
        (and can-access-community?
             (not (require-membership? (get-in community [:description :chats chat-id :permissions])))))))

(fx/defn reset-community-id-input [{:keys [db]} id]
  {:db (assoc db :communities/community-id-input id)})

(defn fetch-community-id-input [{:keys [db]}]
  (:communities/community-id-input db))

(fx/defn import-pressed
  {:events [::import-pressed]}
  [cofx]
  (bottom-sheet/show-bottom-sheet cofx {:view :import-community}))

(fx/defn create-pressed
  {:events [::create-pressed]}
  [cofx]
  (bottom-sheet/show-bottom-sheet cofx {:view :create-community}))

(fx/defn invite-people-pressed
  {:events [::invite-people-pressed]}
  [cofx id]
  (fx/merge cofx
            (reset-community-id-input id)
            (bottom-sheet/show-bottom-sheet {:view :invite-people-community})))

(fx/defn create-channel-pressed
  {:events [::create-channel-pressed]}
  [cofx id]
  (fx/merge cofx
            (reset-community-id-input id)
            (bottom-sheet/show-bottom-sheet {:view :create-community-channel})))

(fx/defn community-created
  {:events [::community-created]}
  [cofx response]
  (fx/merge cofx
            (bottom-sheet/hide-bottom-sheet)
            (handle-response response)))

(fx/defn community-imported
  {:events [::community-imported]}
  [cofx response]
  (fx/merge cofx
            (bottom-sheet/hide-bottom-sheet)
            (handle-response response)))

(fx/defn people-invited
  {:events [::people-invited]}
  [cofx response]
  (fx/merge cofx
            (bottom-sheet/hide-bottom-sheet)
            (handle-response response)))

(fx/defn community-channel-created
  {:events [::community-channel-created]}
  [cofx response]
  (fx/merge cofx
            (bottom-sheet/hide-bottom-sheet)
            (handle-response response)))

(fx/defn handle-export-pressed
  {:events [::export-pressed]}
  [cofx community-id]
  (export cofx community-id
          #(re-frame/dispatch [:show-popover {:view  :export-community
                                              :community-key %}])))

(fx/defn import-confirmation-pressed
  {:events [::import-confirmation-pressed]}
  [cofx community-key]
  (import-community
   cofx
   community-key
   #(re-frame/dispatch [::community-imported %])))

(fx/defn create-confirmation-pressed
  {:events [::create-confirmation-pressed]}
  [cofx community-name community-description membership]
  (create
   cofx
   community-name
   community-description
   membership
   ::community-created
   ::failed-to-create-community))

(fx/defn create-channel-confirmation-pressed
  {:events [::create-channel-confirmation-pressed]}
  [cofx community-channel-name community-channel-description]
  (create-channel
   (fetch-community-id-input cofx)
   community-channel-name
   community-channel-description
   ::community-channel-created
   ::failed-to-create-community-channel))

(fx/defn invite-people-confirmation-pressed
  {:events [::invite-people-confirmation-pressed]}
  [cofx user-pk]
  (invite-user
   cofx
   (fetch-community-id-input cofx)
   user-pk
   ::people-invited
   ::failed-to-invite-people))
