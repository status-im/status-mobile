(ns status-im2.contexts.chat.messages.content.deleted.view
  (:require [quo2.core :as quo]
            [react-native.core :as rn]
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
  [{:keys [deleted-by deleted-undoable-till timestamp-str deleted-for-me-undoable-till from]}
   on-long-press-fn]
  (let [;; deleted message with nil deleted-by is deleted by (:from message)
        display-name (first (rf/sub [:contacts/contact-two-names-by-identity (or deleted-by from)]))
        photo-path   (rf/sub [:chats/photo-path (or deleted-by from)])]
    [quo/system-message
     {:type             :deleted
      :timestamp        timestamp-str
      :child            [user-xxx-deleted-this-message
                         {:display-name display-name :profile-picture photo-path}]
      :on-long-press    on-long-press-fn
      :non-pressable?   (if on-long-press-fn false true)
      :animate-landing? (or deleted-undoable-till deleted-for-me-undoable-till)}]))

(defn deleted-message
  [{:keys [deleted? deleted-by timestamp-str from] :as message}]
  (let [pub-key        (rf/sub [:multiaccount/public-key])
        deleted-by-me? (= (or deleted-by from) pub-key)]
    (if (and deleted? (not deleted-by-me?))
      [deleted-by-message message]
      [quo/system-message
       {:type      :deleted
        :label     (i18n/label
                    (if deleted? :t/message-deleted-for-everyone :t/message-deleted-for-you))
        :timestamp timestamp-str}])))
