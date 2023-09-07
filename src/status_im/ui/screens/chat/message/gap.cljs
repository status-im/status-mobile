(ns status-im.ui.screens.chat.message.gap
  (:require-macros [status-im.utils.views :as views])
  (:require [quo2.core :as quo2]
            [re-frame.core :as re-frame]
            [utils.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.input.gap :as style]
            [utils.datetime :as datetime]))

(views/defview gap
  [{:keys [gap-ids chat-id gap-parameters public? community?]}]
  (views/letsubs [in-progress?      [:chats/fetching-gap-in-progress?
                                     gap-ids
                                     chat-id]
                  connected?        [:mailserver/connected?]
                  use-status-nodes? [:mailserver/use-status-nodes?]
                  first-gap?        (= gap-ids #{:first-gap})
                  window-height     [:dimensions/window-height]]
    (when (or (not first-gap?) public? community?)
      [react/view {:style (when-not in-progress? style/gap-container)}
       [react/touchable-highlight
        {:on-press (when (and (not in-progress?) use-status-nodes? connected?)
                     (re-frame/dispatch [:chat.ui/fill-gaps chat-id gap-ids]))
         :style    {:height (if in-progress? window-height 48)}}
        [react/view {:style style/label-container}
         (if in-progress?
           [quo2/skeleton-list
            {:parent-height window-height
             :animated      true
             :content       :messages}]
           [react/nested-text
            {:style (style/gap-text (and connected? use-status-nodes?))}
            (i18n/label (if first-gap? :t/load-more-messages :t/fetch-messages))
            (when first-gap?
              [{:style style/date}
               (let [date (datetime/timestamp->long-date
                           (* 1000 (:from gap-parameters)))]
                 (str
                  "\n"
                  (i18n/label :t/load-messages-before
                              {:date date})))])])]]])))
