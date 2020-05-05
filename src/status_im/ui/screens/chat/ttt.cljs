
(ns status-im.ui.screens.chat.ttt
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.chat.styles.main :as style]
            [status-im.ui.screens.profile.tribute-to-talk.views
             :as
             tribute-to-talk.views]))

(defn tribute-to-talk-header
  [name]
  [react/nested-text {:style (assoc style/intro-header-description
                                    :margin-bottom 32)}
   (i18n/label :t/tribute-required-by-multiaccount {:multiaccount-name name})
   [{:style    {:color colors/blue}
     :on-press #(re-frame/dispatch [:navigate-to :tribute-learn-more])}
    (str " " (i18n/label :learn-more))]])

(defn pay-to-chat-messages
  [snt-amount chat-id tribute-status tribute-label
   fiat-amount fiat-currency token]
  [tribute-to-talk.views/pay-to-chat-message
   {:snt-amount     snt-amount
    :public-key     chat-id
    :tribute-status tribute-status
    :tribute-label  tribute-label
    :fiat-amount    fiat-amount
    :fiat-currency  fiat-currency
    :token          token
    :style          {:margin-horizontal 8
                     :align-items       :flex-start
                     :align-self        (if snt-amount :flex-start :flex-end)}}])

(defn one-to-one-chat-description-container
  [{:keys                 [chat-id name]
    :tribute-to-talk/keys [received? tribute-status
                           tribute-label snt-amount on-share-my-profile
                           fiat-amount fiat-currency token]}]
  (case tribute-status
    :loading
    [react/view (assoc (dissoc style/empty-chat-container :flex)
                       :justify-content :flex-end)
     [react/view {:style {:align-items :center :justify-content :flex-end}}
      [react/view {:style {:flex-direction :row :justify-content :center}}
       [react/text {:style style/loading-text}
        (i18n/label :t/loading)]
       [react/activity-indicator {:color     colors/gray
                                  :animating true}]]]]

    :required
    [react/view
     [tribute-to-talk-header name]
     [pay-to-chat-messages snt-amount chat-id tribute-status tribute-label
      fiat-amount fiat-currency token]
     [react/view {:style style/are-you-friends-bubble}
      [react/text {:style (assoc style/are-you-friends-text
                                 :font-weight "500")}
       (i18n/label :t/tribute-to-talk-are-you-friends)]
      [react/text {:style style/are-you-friends-text}
       (i18n/label :t/tribute-to-talk-ask-to-be-added)]
      [react/text {:style    style/share-my-profile
                   :on-press on-share-my-profile}
       (i18n/label :t/share-my-profile)]]]

    :pending
    [react/view
     [tribute-to-talk-header name]
     [pay-to-chat-messages snt-amount chat-id tribute-status tribute-label
      fiat-amount fiat-currency token]]

    (:paid :none)
    [react/view
                                        ;[intro-header contact]
     (when (= tribute-status :paid)
       [pay-to-chat-messages snt-amount chat-id tribute-status tribute-label
        fiat-amount fiat-currency token])
     (when received?
       [pay-to-chat-messages nil nil nil nil nil nil nil])

     (when (or (= tribute-status :paid) received?)
       [react/view {:style {:margin-top 16 :margin-horizontal 8}}
        [react/nested-text {:style style/tribute-received-note}
         (when received?
           [{:style (assoc style/tribute-received-note :color colors/gray)}
            (i18n/label :tribute-to-talk-tribute-received1)])
         [{:style (assoc style/tribute-received-note :font-weight "500")}
          name]
         [{:style (assoc style/tribute-received-note :color colors/gray)}
          (i18n/label (if received?
                        :tribute-to-talk-tribute-received2
                        :tribute-to-talk-contact-received-your-tribute))]]])]))

                                        ;[intro-header contact]))
