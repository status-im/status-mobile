(ns status-im2.contexts.shell.activity-center.notification.community-kicked.view
  (:require [quo2.core :as quo]
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

(defn view
  [{:keys [notification set-swipeable-height customization-color] :as props}]
  (let [{:keys [community-id read
                timestamp]} notification
        community           (rf/sub [:communities/community community-id])
        community-name      (:name community)
        community-image     (get-in community [:images :thumbnail :uri])]
    [swipeable props
     [quo/activity-log
      {:title               (i18n/label :t/community-kicked-heading)
       :customization-color customization-color
       :icon                :i/placeholder
       :on-layout           set-swipeable-height
       :timestamp           (datetime/timestamp->relative timestamp)
       :unread?             (not read)
       :context             [[quo/text {:style common-style/user-avatar-tag-text}
                              (i18n/label :t/community-kicked-body)]
                             [quo/context-tag
                              {:type           :community
                               :size           24
                               :blur?          true
                               :community-logo community-image
                               :community-name community-name}]]}]]))
