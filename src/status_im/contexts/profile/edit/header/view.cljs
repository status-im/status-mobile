(ns status-im.contexts.profile.edit.header.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.contexts.profile.edit.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [profile         (rf/sub [:profile/profile-with-image])
        full-name       (profile.utils/displayed-name profile)
        profile-picture (profile.utils/photo profile)]
    [rn/view
     {:key   :edit-profile
      :style style/screen-container}
     [quo/text-combinations {:title (i18n/label :t/edit-profile)}]
     [rn/view style/avatar-wrapper
      [quo/user-avatar
       {:full-name         full-name
        :profile-picture   profile-picture
        :status-indicator? false
        :ring?             true
        :size              :big}]
      [quo/button
       {:on-press        not-implemented/alert
        :container-style style/camera-button
        :icon-only?      true
        :type            :grey
        :background      :photo
        :size            32} :i/camera]]]))
