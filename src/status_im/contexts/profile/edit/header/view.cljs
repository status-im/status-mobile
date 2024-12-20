(ns status-im.contexts.profile.edit.header.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.avatar-picture-picker.view :as profile-picture-picker]
            [status-im.contexts.profile.edit.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [profile               (rf/sub [:profile/profile-with-image])
        customization-color   (rf/sub [:profile/customization-color])
        full-name             (profile.utils/displayed-name profile)
        profile-picture       (profile.utils/photo profile)
        has-picture?          (rf/sub [:profile/has-picture])
        on-change-profile-pic (fn [picture]
                                (if picture
                                  (rf/dispatch [:profile/edit-picture {:picture picture}])
                                  (rf/dispatch [:profile/delete-picture])))]
    [rn/view
     {:key   :edit-profile
      :style style/screen-container}
     [quo/text-combinations {:title (i18n/label :t/edit-profile)}]
     [rn/view style/avatar-wrapper
      [quo/user-avatar
       {:full-name           full-name
        :profile-picture     profile-picture
        :customization-color customization-color
        :status-indicator?   false
        :ring?               true
        :size                :big}]
      [quo/button
       {:on-press        (fn []
                           (rf/dispatch
                            [:show-bottom-sheet
                             {:content (fn []
                                         [profile-picture-picker/view
                                          {:on-result    on-change-profile-pic
                                           :has-picture? has-picture?}])
                              :theme   :dark
                              :shell?  true}]))
        :container-style style/camera-button
        :icon-only?      true
        :type            :grey
        :background      :photo
        :size            32}
       :i/camera]]]))
