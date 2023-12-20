(ns status-im2.contexts.profile.edit.view
  (:require [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im2.common.not-implemented :as not-implemented]
            [status-im2.contexts.profile.edit.style :as style]
            [status-im2.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- internal-view
  [theme]
  (let [insets                           (safe-area/get-insets)
        {:keys [public-key] :as profile} (rf/sub [:profile/profile-with-image])
        full-name                        (profile.utils/displayed-name profile)
        profile-picture                  (profile.utils/photo profile)]
    [quo/overlay {:type :shell}
     [rn/view
      {:key   :header
       :style (style/header-container (:top insets))}
      [quo/page-nav
       {:background :blur
        :icon-name  :i/arrow-left
        :on-press   #(rf/dispatch [:navigate-back])
        :right-side [{:icon-name :i/reveal :on-press not-implemented/alert}]}]]
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
         :size            32} :i/camera]]]]))

(def view (quo.theme/with-theme internal-view))
