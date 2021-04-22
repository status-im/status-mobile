(ns status-im.ui.screens.chat.message.gap
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.utils.datetime :as datetime]
            [status-im.ui.screens.chat.styles.input.gap :as style]))

(defn on-press
  [chat-id gap-ids]
  (fn []
    (re-frame/dispatch [:chat.ui/fill-gaps chat-id gap-ids])))

(views/defview gap
  [{:keys [gap-ids chat-id gap-parameters]}]
  (views/letsubs [in-progress? [:chats/fetching-gap-in-progress?
                                gap-ids
                                chat-id]
                  connected?   [:mailserver/connected?]
                  first-gap?   (= gap-ids #{:first-gap})]
    [react/view {:style (style/gap-container)}
     [react/touchable-highlight
      {:on-press (when (and connected? (not in-progress?))
                   (on-press chat-id gap-ids))
       :style    style/touchable}
      [react/view {:style style/label-container}
       (if in-progress?
         [react/activity-indicator]
         [react/nested-text
          {:style (style/gap-text connected?)}
          (i18n/label (if first-gap? :t/load-more-messages :t/fetch-messages))
          (when first-gap?
            [{:style style/date}
             (let [date (datetime/timestamp->long-date
                         (* 1000 (:from gap-parameters)))]
               (str
                "\n"
                (i18n/label :t/load-messages-before
                            {:date date})))])])]]]))
