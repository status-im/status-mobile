(ns status-im2.contexts.activity-center.notification.community-request.view
  (:require [quo2.core :as quo]
            [status-im2.constants :as constants]
            [status-im2.contexts.activity-center.notification.common.style :as common-style]
            [status-im2.contexts.activity-center.notification.common.view :as common]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- get-header-text-and-context
  [community membership-status timestamp]
  (let [community-name           (:name community)
        community-image          (get-in community [:images :thumbnail :uri])
        community-context-tag    [quo/context-tag common/tag-params {:uri community-image}
                                  community-name]
        requested-within-a-week? (datetime/within-last-n-days? (datetime/to-date timestamp) 7)]
    (cond
      (= membership-status constants/activity-center-membership-status-pending)
      (if requested-within-a-week?
        {:header-text (i18n/label :t/community-request-pending)
         :context     [[quo/text {:style common-style/tag-text}
                        (i18n/label :t/community-request-pending-body-text)]
                       community-context-tag]}
        {:header-text (i18n/label :t/community-request-not-accepted)
         :context     [[quo/text {:style common-style/tag-text}
                        (i18n/label :t/community-request-not-accepted-body-text-prefix)]
                       community-context-tag
                       [quo/text {:style common-style/tag-text}
                        (i18n/label :t/community-request-not-accepted-body-text-suffix)]]})

      (= membership-status constants/activity-center-membership-status-accepted)
      {:header-text (i18n/label :t/community-request-accepted)
       :context     [[quo/text {:style common-style/tag-text}
                      (i18n/label :t/community-request-accepted-body-text)]
                     community-context-tag]}

      :else nil)))

(defn view
  [{:keys [community-id membership-status read timestamp]}]
  (let [community                     (rf/sub [:communities/community community-id])
        {:keys [header-text context]} (get-header-text-and-context community
                                                                   membership-status
                                                                   timestamp)]
    [quo/activity-log
     {:title     header-text
      :icon      :i/communities
      :timestamp (datetime/timestamp->relative timestamp)
      :unread?   (not read)
      :context   context}]))
