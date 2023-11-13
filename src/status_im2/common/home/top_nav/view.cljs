(ns status-im2.common.home.top-nav.view
  (:require
    [quo.core :as quo]
    [status-im2.common.home.top-nav.style :as style]
    [status-im2.constants :as constants]
    [status-im2.contexts.profile.utils :as profile.utils]
    [utils.debounce :refer [dispatch-and-chill]]
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
     {:avatar-on-press          #(rf/dispatch [:navigate-to :my-profile])
      :scan-on-press            #(js/alert "to be implemented")
      :activity-center-on-press #(rf/dispatch [:activity-center/open])
      :qr-code-on-press         #(dispatch-and-chill [:open-modal :share-shell] 1000)
      :container-style          (merge style/top-nav-container container-style)
      :blur?                    blur?
      :jump-to?                 jump-to?
      :customization-color      customization-color
      :avatar-props             avatar
      :max-unread-notifications constants/activity-center-max-unread-count
      :notification-count       unread-count
      :notification             notification-type}]))
