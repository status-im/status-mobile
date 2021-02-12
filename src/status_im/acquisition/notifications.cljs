(ns status-im.acquisition.notifications
  (:require [re-frame.core :as re-frame]
            [status-im.notifications.android :as pn-android]
            [status-im.utils.fx :as fx]
            [status-im.i18n.i18n :as i18n]
            [status-im.notifications.core :as notifications]
            [status-im.ui.components.react :as react]
            [quo.platform :as platform]
            [status-im.ethereum.tokens :as tokens]))

(def channel-id "status-im-referrals")

(re-frame/reg-fx
 ::create-channel
 (fn []
   (when platform/android?
     (pn-android/create-channel {:channel-id   channel-id
                                 :channel-name "Status referrals push notifications"}))))

(fx/defn accept
  [_]
  {::notifications/local-notification
   {:title      (i18n/label :t/starter-pack-coming)
    :message    (i18n/label :t/starter-pack-coming-description)
    :channel-id channel-id
    :icon       (:uri (react/resolve-asset-source tokens/snt-icon-source))}})

(fx/defn claimed
  [_]
  {::notifications/local-notification {:title      (i18n/label :t/starter-pack-received)
                                       :message    (i18n/label :t/starter-pack-received-description)
                                       :channel-id channel-id
                                       :icon       (:uri (react/resolve-asset-source tokens/snt-icon-source))}})
