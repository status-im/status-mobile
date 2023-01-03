(ns status-im2.contexts.shell.events
  (:require [utils.re-frame :as rf]
            [re-frame.core :as re-frame]
            [status-im2.common.constants :as constants]
            [status-im2.navigation.events :as navigation]
            [status-im2.contexts.shell.animation :as animation]
            [status-im2.contexts.shell.constants :as shell.constants]))

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
     (reset! animation/load-communities-stack? (= selected-stack-id :communities-stack))
     (reset! animation/load-chats-stack? (= selected-stack-id :chats-stack))
     (reset! animation/load-wallet-stack? (= selected-stack-id :wallet-stack))
     (reset! animation/load-browser-stack? (= selected-stack-id :browser-stack)))))

;; Events

(rf/defn add-switcher-card
  {:events [:shell/add-switcher-card]}
  [{:keys [db now] :as cofx} view-id id]
  (case view-id
    :chat
    (let [chat (get-in db [:chats id])]
      (case (:chat-type chat)
        constants/one-to-one-chat-type
        {:shell/navigate-from-shell-fx :chats-stack
         :db                           (assoc-in
                                        db
                                        [:shell/switcher-cards id]
                                        {:type  shell.constants/one-to-one-chat-card
                                         :id    id
                                         :clock now})}

        constants/private-group-chat-type
        {:shell/navigate-from-shell-fx :chats-stack
         :db                           (assoc-in
                                        db
                                        [:shell/switcher-cards id]
                                        {:type  shell.constants/private-group-chat-card
                                         :id    id
                                         :clock now})}

        constants/community-chat-type
        {:shell/navigate-from-shell-fx :communities-stack
         :db                           (assoc-in
                                        db
                                        [:shell/switcher-cards (:community-id chat)]
                                        {:type       shell.constants/community-channel-card
                                         :id         (:community-id chat)
                                         :clock      now
                                         :channel-id (:chat-id chat)})}

        nil))

    :community
    {:shell/navigate-from-shell-fx :communities-stack
     :db                           (assoc-in
                                    db
                                    [:shell/switcher-cards (:community-id id)]
                                    {:type       shell.constants/community-card
                                     :id         (:community-id id)
                                     :clock      now
                                     :channel-id nil})}

    nil))

(rf/defn close-switcher-card
  {:events [:shell/close-switcher-card]}
  [{:keys [db]} id]
  {:db (update-in db [:shell/switcher-cards] dissoc id)})

(rf/defn navigate-to-jump-to
  {:events [:shell/navigate-to-jump-to]}
  [cofx]
  (rf/merge
   cofx
   {:shell/navigate-to-jump-to-fx nil}
   (navigation/pop-to-root-tab :shell-stack)))
