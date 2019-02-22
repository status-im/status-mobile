(ns status-im.ui.screens.chat.message.gap
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.datetime :as datetime]
            [status-im.ui.screens.chat.styles.input.gap :as style]))

(defn on-press
  [ids first-gap? idx list-ref]
  (fn []
    (when (and list-ref @list-ref)
      (.scrollToIndex @list-ref
                      #js {:index        (max 0 (dec idx))
                           :viewOffset   20
                           :viewPosition 0.5}))
    (if first-gap?
      (re-frame/dispatch [:chat.ui/fetch-more])
      (re-frame/dispatch [:chat.ui/fill-gaps ids]))))

(views/defview gap
  [{:keys [gaps first-gap?]} idx list-ref]
  (views/letsubs [{:keys [range intro-status]} [:chats/current-chat]
                  in-progress? [:chats/fetching-gap-in-progress?
                                (if first-gap?
                                  [:first-gap]
                                  (:ids gaps))]
                  connected?   [:mailserver/connected?]]
    (let [ids            (:ids gaps)
          intro-loading? (= intro-status :loading)]
      (when-not (and first-gap? intro-loading?)
        [react/view {:style style/gap-container}
         [react/touchable-highlight
          {:on-press (when (and connected? (not in-progress?))
                       (on-press ids first-gap? idx list-ref))
           :style    style/touchable}
          [react/view {:style style/label-container}
           (if in-progress?
             [react/activity-indicator]
             [react/nested-text
              {:style (style/gap-text connected?)}
              (i18n/label (if first-gap?
                            :t/load-more-messages
                            :t/fetch-messages))
              (when first-gap?
                [{:style style/date}
                 (let [date (datetime/timestamp->long-date
                             (* 1000 (:lowest-request-from range)))]
                   (str
                    "\n"
                    (i18n/label :t/load-messages-before
                                {:date date})))])])]]]))))
