(ns status-im2.contexts.shell.activity-center.notification.community-request.view
  (:require [quo2.core :as quo]
            [status-im2.constants :as constants]
            [status-im2.contexts.shell.activity-center.notification.common.style :as common-style]
            [status-im2.contexts.shell.activity-center.notification.common.view :as common]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- swipeable
  [{:keys [active-swipeable extra-fn]} child]
  [common/swipeable
   {:left-button      common/swipe-button-read-or-unread
    :left-on-press    common/swipe-on-press-toggle-read
    :right-button     common/swipe-button-delete
    :right-on-press   common/swipe-on-press-delete
    :active-swipeable active-swipeable
    :extra-fn         extra-fn}
   child])

(defn- get-header-text-and-context
  [community membership-status]
  (let [community-name        (:name community)
        community-image       (get-in community [:images :thumbnail :uri])
        community-context-tag [quo/context-tag common/tag-params community-image
                               community-name]]
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
      {:header-text (i18n/label :t/community-request-accepted)
       :context     [[quo/text {:style common-style/user-avatar-tag-text}
                      (i18n/label :t/community-request-accepted-body-text)]
                     community-context-tag]}

      :else nil)))

(defn view
  [{:keys [notification set-swipeable-height customization-color] :as props}]
  (let [{:keys [community-id membership-status read
                timestamp]}           notification
        community                     (rf/sub [:communities/community community-id])
        {:keys [header-text context]} (get-header-text-and-context community
                                                                   membership-status)]
    [swipeable props
     [quo/activity-log
      {:title               header-text
       :customization-color customization-color
       :icon                :i/communities
       :on-layout           set-swipeable-height
       :timestamp           (datetime/timestamp->relative timestamp)
       :unread?             (not read)
       :context             context}]]))
