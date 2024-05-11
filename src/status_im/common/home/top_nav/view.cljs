(ns status-im.common.home.top-nav.view
  (:require
    [quo.core :as quo]
    [status-im.common.home.top-nav.style :as style]
    [status-im.constants :as constants]
    [status-im.contexts.profile.utils :as profile.utils]
    [utils.re-frame :as rf]))

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
                                           nil)]
    [quo/top-nav
     {:avatar-on-press          #(rf/dispatch [:open-modal :settings])
      :scan-on-press            #(rf/dispatch [:open-modal :shell-qr-reader])
      :activity-center-on-press #(rf/dispatch [:activity-center/open])
      :qr-code-on-press         #(rf/dispatch [:open-modal :screen/share-shell])
      :container-style          (merge style/top-nav-container container-style)
      :blur?                    blur?
      :jump-to?                 jump-to?
      :customization-color      customization-color
      :avatar-props             avatar
      :max-unread-notifications constants/activity-center-max-unread-count
      :notification-count       unread-count
      :notification             notification-type}]))
