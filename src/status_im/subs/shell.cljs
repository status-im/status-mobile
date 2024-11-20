(ns status-im.subs.shell
  (:require
    [re-frame.core :as re-frame]
    [status-im.constants :as constants]))

;; Bottom tabs
(re-frame/reg-sub
 :shell/bottom-tabs-notifications-data
 :<- [:chats/chats]
 (fn [chats]
   (let [{:keys [chats-stack community-stack]}
         (reduce
          (fn [acc [_ {:keys [unviewed-messages-count unviewed-mentions-count chat-type muted]}]]
            (cond
              (and (not muted)
                   (= chat-type constants/community-chat-type))
              (-> acc
                  (update-in [:community-stack :unviewed-messages-count] + unviewed-messages-count)
                  (update-in [:community-stack :unviewed-mentions-count] + unviewed-mentions-count))

              (and (not muted)
                   (= chat-type constants/private-group-chat-type))
              (-> acc
                  (update-in [:chats-stack :unviewed-messages-count] + unviewed-messages-count)
                  (update-in [:chats-stack :unviewed-mentions-count] + unviewed-mentions-count))

              (and (not muted)
                   (= chat-type constants/one-to-one-chat-type))
              ;; Note - for 1-1 chats, all unread messages are counted as mentions and shown with
              ;; counter
              (-> acc
                  (update-in [:chats-stack :unviewed-messages-count] + unviewed-messages-count)
                  (update-in [:chats-stack :unviewed-mentions-count] + unviewed-messages-count))

              :else
              acc))
          {:chats-stack     {:unviewed-messages-count 0 :unviewed-mentions-count 0}
           :community-stack {:unviewed-messages-count 0 :unviewed-mentions-count 0}}
          chats)]
     {:communities-stack
      {:new-notifications?     (pos? (:unviewed-messages-count community-stack))
       :notification-indicator (if (pos? (:unviewed-mentions-count community-stack))
                                 :counter
                                 :unread-dot)
       :counter-label          (:unviewed-mentions-count community-stack)}
      :chats-stack
      {:new-notifications?     (pos? (:unviewed-messages-count chats-stack))
       :notification-indicator (if (pos? (:unviewed-mentions-count chats-stack))
                                 :counter
                                 :unread-dot)
       :counter-label          (:unviewed-mentions-count chats-stack)}})))
