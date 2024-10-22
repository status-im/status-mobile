(ns status-im.common.home.top-nav.view
  (:require
    [cljs-time.core :as t]
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.common.home.top-nav.style :as style]
    [status-im.constants :as constants]
    [status-im.contexts.profile.utils :as profile.utils]
    [utils.datetime :as datetime]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- network-status-label
  [now wallet-latest-update]
  (when (t/after? now (t/plus wallet-latest-update (t/minutes 5)))
    (let [units    [{:name :t/datetime-second-short :limit 60 :in-second 1}
                    {:name :t/datetime-minute-mid :limit 3600 :in-second 60}
                    {:name :t/datetime-hour-short-lowercase :limit 86400 :in-second 3600}
                    {:name :t/datetime-day :limit nil :in-second 86400}]
          time-ago (datetime/time-ago-long wallet-latest-update units)]
      [rn/view {:style {:justify-content :center}}
       [quo/network-status-tag {:label (i18n/label :t/wallet-updated-at {:at time-ago})}]])))

(defn view
  "[top-nav props]
  props
  {:blur? true/false
   :jump-to? true/false
   :container-style passed to outer view of component}"
  [{:keys [container-style blur? jump-to?]}]
  (let [{:keys [public-key] :as profile} (rf/sub [:profile/profile-with-image])
        online?                          (rf/sub [:visibility-status-updates/online?
                                                  public-key])
        customization-color              (rf/sub [:profile/customization-color])
        wallet-latest-update             (rf/sub [:wallet/latest-update])
        wallet-blockchain-status         (rf/sub [:wallet/blockchain-status])
        avatar                           {:online?         online?
                                          :full-name       (profile.utils/displayed-name profile)
                                          :profile-picture (profile.utils/photo profile)}

        unread-count                     (rf/sub [:activity-center/unread-count])
        indicator                        (rf/sub [:activity-center/unread-indicator])
        notification-type                (case indicator
                                           ; should be `seen` TODO discuss with design team
                                           ; notifications for activity centre
                                           :unread-indicator/seen :mention-seen
                                           ; should be :notification TODO
                                           ; https://github.com/status-im/status-mobile/issues/17102
                                           :unread-indicator/new  :mention
                                           nil)
        view-id                          (rf/sub [:view-id])
        initial-share-tab                (if (= :wallet-stack view-id) :wallet :profile)]
    [quo/top-nav
     {:avatar-on-press          #(rf/dispatch [:open-modal :settings])
      :scan-on-press            #(rf/dispatch [:open-modal :shell-qr-reader])
      :activity-center-on-press #(rf/dispatch [:activity-center/open])
      :right-section-content    (when (and (= (:status wallet-blockchain-status) "down")
                                           wallet-latest-update)
                                  [network-status-label (t/now) wallet-latest-update])
      :qr-code-on-press         #(rf/dispatch [:open-modal :screen/share-shell
                                               {:initial-tab initial-share-tab}])
      :container-style          (merge style/top-nav-container container-style)
      :blur?                    blur?
      :jump-to?                 jump-to?
      :customization-color      customization-color
      :avatar-props             avatar
      :max-unread-notifications constants/activity-center-max-unread-count
      :notification-count       unread-count
      :notification             notification-type}]))
