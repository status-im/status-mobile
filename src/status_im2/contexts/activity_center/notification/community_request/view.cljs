(ns status-im2.contexts.activity-center.notification.community-request.view
  (:require [quo2.core :as quo]
            [status-im2.contexts.activity-center.notification.common.style :as common-style]
            [status-im2.contexts.activity-center.notification.common.view :as common]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- get-header-and-body-text
  [membership-status]
  (case membership-status
    1 {:header-text (i18n/label :t/community-request-pending)
       :body-text   (i18n/label :t/community-request-pending-body-text)}

    2 {:header-text (i18n/label :t/community-request-accepted)
       :body-text   (i18n/label :t/community-request-accepted-body-text)}

    3 {:header-text (i18n/label :t/community-request-declined)
       :body-text   (i18n/label :t/community-request-declined=body-text)}

    nil))

(defn view
  [{:keys [community-id membership-status read timestamp]}]
  (let [{:keys [header-text body-text]} (get-header-and-body-text membership-status)
        community                       (rf/sub [:communities/community community-id])
        community-name                  (:name community)
        community-image                 (get-in community [:images :thumbnail :uri])]
    [quo/activity-log
     {:title     header-text
      :icon      :i/communities
      :timestamp (datetime/timestamp->relative timestamp)
      :unread?   (not read)
      :context   [[quo/text {:style common-style/tag-text} body-text]
                  [quo/context-tag common/tag-params {:uri community-image} community-name]]}]))