(ns status-im.contexts.chat.messenger.messages.content.deleted.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn user-xxx-deleted-this-message
  [{:keys [display-name profile-picture]}]
  [rn/view
   {:style {:flex-direction :row
            :align-items    :center
            :flex-shrink    1}}
   [rn/view
    {:style {:margin-right 4
             :align-self   :center}}
    [quo/user-avatar
     {:full-name         display-name
      :profile-picture   profile-picture
      :status-indicator? false
      :size              :xxxs}]]
   [quo/text
    {:weight          :semi-bold
     :number-of-lines 1
     :style           {:flex-shrink 1 :margin-right 4}
     :size            :paragraph-2}
    display-name]
   [quo/text
    {:size            :paragraph-2
     :flex-shrink     0
     :number-of-lines 1}
    (i18n/label :t/deleted-this-message)]])

(defn deleted-by-message
  [{:keys [deleted-by timestamp-str from on-long-press animation-duration]}]
  (let [;; deleted message with nil deleted-by is deleted by (:from message)
        [primary-name _] (rf/sub [:contacts/contact-two-names-by-identity (or deleted-by from)])
        photo-path       (rf/sub [:chats/photo-path (or deleted-by from)])]
    [quo/system-message
     {:type                        :deleted
      :animate-bg-color?           animation-duration
      :bg-color-animation-duration animation-duration
      :on-long-press               on-long-press
      :timestamp                   timestamp-str
      :child                       [user-xxx-deleted-this-message
                                    {:display-name primary-name :profile-picture photo-path}]}]))

(defn deleted-message
  [{:keys [deleted? deleted-for-me? deleted-by pinned timestamp-str from
           on-long-press deleted-undoable-till deleted-for-me-undoable-till]
    :as   message}
   {:keys [message-pin-enabled in-pinned-view?]}]
  (let [pub-key            (rf/sub [:multiaccount/public-key])
        deleted-by-me?     (= (or deleted-by from) pub-key)
        animation-duration (when-let [deleted-till (or deleted-undoable-till
                                                       deleted-for-me-undoable-till)]
                             (- deleted-till (datetime/timestamp)))
        ;; enable long press only when undo delete timer timedout
        ;; message pinned and user has permission to unpin
        on-long-press      (when (and (not animation-duration)
                                      (or (and (or in-pinned-view? pinned) message-pin-enabled)
                                          (and (not deleted?) deleted-for-me?)))
                             on-long-press)]
    (if (and deleted? (not deleted-by-me?))
      [deleted-by-message
       (assoc message
              :on-long-press      on-long-press
              :animation-duration animation-duration)]
      [quo/system-message
       {:type                        :deleted
        :animate-bg-color?           animation-duration
        :bg-color-animation-duration animation-duration
        :on-long-press               on-long-press
        :label                       (i18n/label
                                      (if deleted?
                                        :t/message-deleted-for-everyone
                                        :t/message-deleted-for-you))
        :timestamp                   timestamp-str}])))
