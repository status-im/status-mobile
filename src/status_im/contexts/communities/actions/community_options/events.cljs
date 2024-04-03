(ns status-im.contexts.communities.actions.community-options.events
  (:require [status-im.common.muting.helpers :as muting.helpers]
            [status-im.constants :as constants]
            [taoensso.timbre :as log]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(rf/reg-event-fx :community/update-community-chats-mute-status
 (fn [{:keys [db]} [community-id muted? muted-till]]
   {:db
    (reduce
     #(update-in %1 [:chats (str community-id %2)] assoc :muted muted? :muted-till muted-till)
     db
     (keys (get-in db [:communities community-id :chats])))}))

(rf/reg-event-fx
 :community/mute-community-failed
 (fn [{:keys [db]} [community-id muted? error]]
   (log/error "mute community failed" community-id error)
   {:db       (assoc-in db [:communities community-id :muted] (not muted?))
    :dispatch [:community/update-community-chats-mute-status community-id muted? error]}))

(rf/reg-event-fx :community/mute-community-successful
 (fn [{:keys [db]} [community-id muted? muted-till]]
   (let [time-string (fn [mute-title mute-duration]
                       (i18n/label mute-title {:duration mute-duration}))]
     {:db (assoc-in db [:communities community-id :muted-till] muted-till)
      :fx [[:dispatch [:community/update-community-chats-mute-status community-id muted? muted-till]]
           [:dispatch
            [:toasts/upsert
             (cond-> {:type :positive
                      :id   :mute-community-toast
                      :text (if muted?
                              (when (some? muted-till)
                                (time-string :t/muted-until
                                             (muting.helpers/format-mute-till muted-till)))
                              (i18n/label :t/community-unmuted))}
               muted? (assoc :duration      constants/mute-undo-time-limit-ms
                             :undo-duration (/ constants/mute-undo-time-limit-ms 1000)
                             :undo-on-press #(rf/dispatch [:community/undo-mute
                                                           community-id])))]]]})))

(rf/reg-event-fx
 :community/undo-mute
 (fn [_ [community-id]]
   {:fx [[:json-rpc/call
          [{:method     "wakuext_unMuteCommunityChats"
            :params     [community-id]
            :on-error   #(rf/dispatch [:community/mute-community-failed community-id false %])
            :on-success #(rf/dispatch [:community/undo-mute-success community-id])}]]]}))

(rf/reg-event-fx
 :community/undo-mute-success
 (fn [{:keys [db]} [community-id]]
   {:db (update-in db
                   [:communities community-id]
                   (fn [community-id]
                     (-> community-id
                         (dissoc :muted-till)
                         (assoc :muted false))))
    :fx [[:dispatch [:toasts/close :mute-community-toast]]
         [:dispatch [:community/update-community-chats-mute-status community-id false nil]]]}))

(rf/reg-event-fx :community/set-muted
 (fn [{:keys [db]} [community-id muted? muted-type]]
   (let [params (if muted? [{:communityId community-id :mutedType muted-type}] [community-id])
         method (if muted? "wakuext_muteCommunityChats" "wakuext_unMuteCommunityChats")]
     {:db            (assoc-in db [:communities community-id :muted] muted?)
      :json-rpc/call [{:method     method
                       :params     params
                       :on-error   #(rf/dispatch [:community/mute-community-failed community-id
                                                  muted? %])
                       :on-success #(rf/dispatch [:community/mute-community-successful
                                                  community-id muted? %])}]})))
