(ns status-im.contexts.shell.activity-center.notification.community-request.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.gesture :as gesture]
    [status-im.constants :as constants]
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

(defn- get-header-text-and-context
  [community-logo community-name community-permissions membership-status]
  (let [open?                 (not= 3 (:access community-permissions))
        community-context-tag [quo/context-tag
                               {:type           :community
                                :size           24
                                :blur?          true
                                :community-logo community-logo
                                :community-name community-name}]]
    (cond
      (= membership-status constants/activity-center-membership-status-idle)
      {:header-text (i18n/label :t/community-request-not-accepted)
       :context     [[quo/text {:style common-style/user-avatar-tag-text}
                      (i18n/label :t/community-request-not-accepted-body-text-prefix)]
                     community-context-tag
                     [quo/text {:style common-style/user-avatar-tag-text}
                      (i18n/label :t/community-request-not-accepted-body-text-suffix)]]}

      (= membership-status constants/activity-center-membership-status-pending)
      {:header-text (i18n/label :t/community-request-pending)
       :context     [[quo/text {:style common-style/user-avatar-tag-text}
                      (i18n/label :t/community-request-pending-body-text)]
                     community-context-tag]}

      (= membership-status constants/activity-center-membership-status-accepted)
      {:header-text (i18n/label (if open?
                                  :t/join-open-community
                                  :t/community-request-accepted))
       :context     [[quo/text {:style common-style/user-avatar-tag-text}
                      (i18n/label (if open?
                                    :t/joined-community
                                    :t/community-request-accepted-body-text)
                                  (when open? {:community community-name}))]
                     community-context-tag]})))

(defn view
  [{:keys [notification extra-fn]}]
  (let [{:keys [community-id membership-status read
                timestamp]}   notification
        community-name        (rf/sub [:communities/name community-id])
        community-logo        (rf/sub [:communities/logo community-id])
        community-permissions (rf/sub [:communities/permissions community-id])
        customization-color   (rf/sub [:profile/customization-color])
        {:keys [header-text
                context]}     (get-header-text-and-context community-logo
                                                           community-name
                                                           community-permissions
                                                           membership-status)
        on-press              (rn/use-callback
                               (fn []
                                 (rf/dispatch [:navigate-back])
                                 (rf/dispatch [:communities/navigate-to-community-overview
                                               community-id]))
                               [community-id])]
    [swipeable {:extra-fn extra-fn}
     [gesture/touchable-without-feedback {:on-press on-press}
      [quo/activity-log
       {:title               header-text
        :customization-color customization-color
        :icon                :i/communities
        :timestamp           (datetime/timestamp->relative timestamp)
        :unread?             (not read)
        :context             context}]]]))
