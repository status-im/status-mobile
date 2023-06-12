(ns status-im2.contexts.shell.events
  (:require [utils.re-frame :as rf]
            [re-frame.core :as re-frame]
            [status-im.utils.core :as utils]
            [status-im2.constants :as constants]
            [status-im2.contexts.shell.state :as state]
            [status-im2.contexts.shell.utils :as shell.utils]
            [status-im2.navigation.state :as navigation.state]
            [status-im2.contexts.shell.animation :as animation]
            [status-im2.contexts.shell.constants :as shell.constants]
            [status-im.data-store.switcher-cards :as switcher-cards-store]))

;;;; Effects

;; Navigation
(re-frame/reg-fx
 :shell/change-tab-fx
 (fn [stack-id]
   (when (some #(= stack-id %) shell.constants/stacks-ids)
     (animation/bottom-tab-on-press stack-id false))))

(re-frame/reg-fx
 :shell/navigate-to-jump-to-fx
 (fn []
   (animation/close-home-stack false)
   (some-> ^js @state/jump-to-list-ref
           (.scrollToOffset #js {:y 0 :animated false}))))

(re-frame/reg-fx
 :shell/pop-to-root-fx
 (fn []
   (reset! state/floating-screens-state {})))

(re-frame/reg-fx
 :shell/reset-state
 (fn []
   (reset! state/floating-screens-state {})))

;;;; Events

;; Switcher
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
        {:card-id       id
         :switcher-card {:type      shell.constants/one-to-one-chat-card
                         :card-id   id
                         :clock     now
                         :screen-id id}}

        constants/private-group-chat-type
        {:card-id       id
         :switcher-card {:type      shell.constants/private-group-chat-card
                         :card-id   id
                         :clock     now
                         :screen-id id}}

        constants/community-chat-type
        {:card-id       (:community-id chat)
         :switcher-card {:type      shell.constants/community-channel-card
                         :card-id   (:community-id chat)
                         :clock     now
                         :screen-id (:chat-id chat)}}

        nil))

    :community-overview
    {:card-id       id
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
       {:db (assoc-in
             db
             [:shell/switcher-cards (:card-id card-data)]
             switcher-card)}
       (switcher-cards-store/upsert-switcher-card-rpc switcher-card)))))

(rf/defn close-switcher-card
  {:events [:shell/close-switcher-card]}
  [{:keys [db] :as cofx} card-id]
  (rf/merge
   cofx
   {:db (update db :shell/switcher-cards dissoc card-id)}
   (switcher-cards-store/delete-switcher-card-rpc card-id)))

;; Navigation
(rf/defn navigate-to-jump-to
  {:events [:shell/navigate-to-jump-to]}
  [{:keys [db]}]
  (let [chat-screen-open?      (shell.utils/floating-screen-open? shell.constants/chat-screen)
        community-screen-open? (shell.utils/floating-screen-open? shell.constants/community-screen)]
    {:db
     (cond-> db

       chat-screen-open?
       (assoc-in [:shell/floating-screens shell.constants/chat-screen :animation]
        shell.constants/close-screen-with-shell-animation)

       (and chat-screen-open? community-screen-open?)
       (assoc-in [:shell/floating-screens shell.constants/community-screen :animation]
        shell.constants/close-screen-without-animation)

       (and (not chat-screen-open?) community-screen-open?)
       (assoc-in [:shell/floating-screens shell.constants/community-screen :animation]
        shell.constants/close-screen-with-shell-animation))

     :dispatch [:set-view-id :shell]
     :shell/navigate-to-jump-to-fx nil}))

(rf/defn change-shell-status-bar-style
  {:events [:change-shell-status-bar-style]}
  [_ style]
  {:merge-options {:id "shell-stack" :options {:statusBar {:style style}}}})

(rf/defn change-shell-nav-bar-color
  {:events [:change-shell-nav-bar-color]}
  [_ color]
  {:merge-options {:id "shell-stack" :options {:navigationBar {:backgroundColor color}}}})

(rf/defn shell-navigate-to
  {:events [:shell/navigate-to]}
  [{:keys [db]} go-to-view-id screen-params animation hidden-screen?]
  (if (shell.utils/shell-navigation? go-to-view-id)
    (let [current-view-id (:view-id db)
          community-id    (get-in db [:chats screen-params :community-id])]
      {:db         (assoc-in
                    db
                    [:shell/floating-screens go-to-view-id]
                    {:id             screen-params
                     :community-id   community-id
                     :hidden-screen? hidden-screen?
                     :animation      (or animation
                                         (case current-view-id
                                           :shell shell.constants/open-screen-with-shell-animation
                                           :chat  shell.constants/open-screen-without-animation
                                           shell.constants/open-screen-with-slide-animation))})
       :dispatch-n (cond-> []
                     (not hidden-screen?)
                     (conj [:set-view-id go-to-view-id])
                     (and (= go-to-view-id shell.constants/community-screen)
                          (not hidden-screen?)
                          (:current-chat-id db))
                     (conj [:chat/close]))})
    {:db          (assoc db :view-id go-to-view-id)
     :navigate-to go-to-view-id}))

(rf/defn shell-navigate-back
  {:events [:shell/navigate-back]}
  [{:keys [db]}]
  (let [chat-screen-open?      (shell.utils/floating-screen-open? shell.constants/chat-screen)
        community-screen-open? (shell.utils/floating-screen-open? shell.constants/community-screen)
        current-chat-id        (:current-chat-id db)
        community-id           (when current-chat-id
                                 (get-in db [:chats current-chat-id :community-id]))]
    (if (and (not @navigation.state/curr-modal)
             (or chat-screen-open? community-screen-open?))
      {:db         (assoc-in
                    db
                    [:shell/floating-screens
                     (if chat-screen-open? shell.constants/chat-screen shell.constants/community-screen)
                     :animation]
                    shell.constants/close-screen-with-slide-animation)
       :dispatch-n (cond-> [[:set-view-id
                             (cond
                               (and chat-screen-open? community-screen-open?)
                               shell.constants/community-screen
                               community-screen-open?
                               :communities-stack
                               :else :chats-stack)]]
                     ;; When navigating back from community chat to community, update switcher card
                     (and chat-screen-open? community-screen-open? community-id)
                     (conj [:shell/add-switcher-card shell.constants/community-screen community-id]))}
      {:navigate-back nil})))

(rf/defn floating-screen-opened
  {:events [:shell/floating-screen-opened]}
  [{:keys [db]} screen-id id community-id hidden-screen?]
  (merge
   {:db                  (assoc-in db [:shell/loaded-screens screen-id] true)
    :shell/change-tab-fx (if (or (= screen-id shell.constants/community-screen)
                                 community-id)
                           :communities-stack
                           :chats-stack)}
   (when community-id
     ;; When opening community chat, open community screen in background
     {:dispatch [:shell/navigate-to shell.constants/community-screen
                 community-id shell.constants/open-screen-without-animation true]})
   ;; Only update switcher cards for top screen
   (when-not hidden-screen?
     {:dispatch-later [{:ms       (* 2 shell.constants/shell-animation-time)
                        :dispatch [:shell/add-switcher-card screen-id id]}]})))

(rf/defn floating-screen-closed
  {:events [:shell/floating-screen-closed]}
  [{:keys [db]} screen-id]
  (merge
   {:db (-> (update db :shell/floating-screens dissoc screen-id)
            (update :shell/loaded-screens dissoc screen-id))}
   (when (= screen-id shell.constants/chat-screen)
     {:dispatch [:chat/close]})))
