(ns status-im2.contexts.shell.events
  (:require [utils.re-frame :as rf]
            [re-frame.core :as re-frame]
            [status-im.utils.core :as utils]
            [status-im2.constants :as constants]
            [status-im2.navigation.events :as navigation]
            [status-im.async-storage.core :as async-storage]
            [status-im2.contexts.shell.animation :as animation]
            [status-im2.contexts.shell.constants :as shell.constants]
            [status-im.data-store.switcher-cards :as switcher-cards-store]))

;; Effects

(re-frame/reg-fx
 :shell/navigate-to-jump-to-fx
 (fn []
   (animation/close-home-stack false)))

(re-frame/reg-fx
 :shell/navigate-from-shell-fx
 (fn [stack-id]
   (js/setTimeout #(animation/bottom-tab-on-press stack-id) 500)))

(re-frame/reg-fx
 :shell/reset-bottom-tabs
 (fn []
   (let [selected-stack-id @animation/selected-stack-id]
     (async-storage/set-item! :selected-stack-id nil)
     (reset! animation/load-communities-stack? (= selected-stack-id :communities-stack))
     (reset! animation/load-chats-stack? (= selected-stack-id :chats-stack))
     (reset! animation/load-wallet-stack? (= selected-stack-id :wallet-stack))
     (reset! animation/load-browser-stack? (= selected-stack-id :browser-stack)))))

;; Events

(rf/defn switcher-cards-loaded
  {:events [:shell/switcher-cards-loaded]}
  [{:keys [db]} loaded-switcher-cards]
  {:db (assoc db
              :shell/switcher-cards
              (utils/index-by :card-id (switcher-cards-store/<-rpc loaded-switcher-cards)))})

(defn calculate-card-data
  [db now view-id id]
  (case view-id
    :chat
    (let [chat (get-in db [:chats id])]
      (case (:chat-type chat)
        constants/one-to-one-chat-type
        {:navigate-from :chats-stack
         :card-id       id
         :switcher-card {:type      shell.constants/one-to-one-chat-card
                         :card-id   id
                         :clock     now
                         :screen-id id}}

        constants/private-group-chat-type
        {:navigate-from :chats-stack
         :card-id       id
         :switcher-card {:type      shell.constants/private-group-chat-card
                         :card-id   id
                         :clock     now
                         :screen-id id}}

        constants/community-chat-type
        {:navigate-from :communities-stack
         :card-id       (:community-id chat)
         :switcher-card {:type      shell.constants/community-channel-card
                         :card-id   (:community-id chat)
                         :clock     now
                         :screen-id (:chat-id chat)}}

        nil))

    :community-overview
    {:navigate-from :communities-stack
     :card-id       id
     :switcher-card {:type      shell.constants/community-card
                     :card-id   id
                     :clock     now
                     :screen-id id}}
    nil))

(rf/defn add-switcher-card
  {:events [:shell/add-switcher-card]}
  [{:keys [db now] :as cofx} view-id id]
  (let [card-data     (calculate-card-data db now view-id id)
        switcher-card (:switcher-card card-data)]
    (when card-data
      (rf/merge
       cofx
       {:db                           (assoc-in
                                       db
                                       [:shell/switcher-cards (:card-id card-data)]
                                       switcher-card)
        :shell/navigate-from-shell-fx (:navigate-from card-data)}
       (switcher-cards-store/upsert-switcher-card-rpc switcher-card)))))

(rf/defn close-switcher-card
  {:events [:shell/close-switcher-card]}
  [{:keys [db] :as cofx} card-id]
  (rf/merge
   cofx
   {:db (update db :shell/switcher-cards dissoc card-id)}
   (switcher-cards-store/delete-switcher-card-rpc card-id)))

(rf/defn navigate-to-jump-to
  {:events [:shell/navigate-to-jump-to]}
  [cofx]
  (rf/merge
   cofx
   {:shell/navigate-to-jump-to-fx nil}
   (navigation/pop-to-root-tab :shell-stack)))
