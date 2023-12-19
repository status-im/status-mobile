(ns status-im.contexts.chat.lightbox.events
  (:require status-im.contexts.chat.lightbox.effects
            [utils.re-frame :as rf]))

(rf/reg-event-fx :chat.ui/clear-sending-images
 (fn [{:keys [db]}]
   {:db (update-in db [:chat/inputs (:current-chat-id db) :metadata] assoc :sending-image {})}))

(rf/reg-event-fx :chat.ui/image-unselected
 (fn [{:keys [db]} [original]]
   (let [current-chat-id (:current-chat-id db)]
     {:db (update-in db
                     [:chat/inputs current-chat-id :metadata :sending-image]
                     dissoc
                     (:uri original))})))

(rf/reg-event-fx :chat.ui/share-image
 (fn [_ [uri]]
   {:effects.chat/share-image uri}))

(rf/reg-event-fx :chat.ui/save-image-to-gallery
 (fn [_ [uri on-success]]
   {:effects.chat/save-image-to-gallery [uri on-success]}))
