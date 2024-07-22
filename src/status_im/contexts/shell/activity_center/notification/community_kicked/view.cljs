(ns status-im.contexts.shell.activity-center.notification.community-kicked.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im.contexts.shell.activity-center.notification.common.style :as common-style]
    [status-im.contexts.shell.activity-center.notification.common.view :as common]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- swipeable
  [{:keys [extra-fn]} child]
  [common/swipeable
   {:left-button    common/swipe-button-read-or-unread
    :left-on-press  common/swipe-on-press-toggle-read
    :right-button   common/swipe-button-delete
    :right-on-press common/swipe-on-press-delete
    :extra-fn       extra-fn}
   child])

(defn view
  [{:keys [notification extra-fn]}]
  (let [{:keys [id community-id read
                timestamp]} notification
        community           (rf/sub [:communities/community community-id])
        community-name      (:name community)
        community-image     (get-in community [:images :thumbnail :uri])
        customization-color (rf/sub [:profile/customization-color])
        on-press            (rn/use-callback
                             (fn []
                               (rf/dispatch [:navigate-back])
                               (rf/dispatch [:activity-center.notifications/mark-as-read id]))
                             [id])]
    [swipeable {:extra-fn extra-fn}
     [gesture/touchable-without-feedback {:on-press on-press}
      [quo/activity-log
       {:title               (i18n/label :t/community-kicked-heading)
        :customization-color customization-color
        :icon                :i/placeholder
        :timestamp           (datetime/timestamp->relative timestamp)
        :unread?             (not read)
        :context             [[quo/text {:style common-style/user-avatar-tag-text}
                               (i18n/label :t/community-kicked-body)]
                              [quo/context-tag
                               {:type           :community
                                :size           24
                                :blur?          true
                                :community-logo community-image
                                :community-name community-name}]]}]]]))
