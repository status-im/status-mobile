(ns status-im.contexts.profile.settings.header.view
  (:require [quo.core :as quo]
            [quo.theme]
            [react-native.core :as rn]
            [status-im.common.scalable-avatar.view :as avatar]
            [status-im.contexts.profile.settings.header.header-shape :as header.shape]
            [status-im.contexts.profile.settings.header.style :as style]
            [status-im.contexts.profile.settings.header.utils :as header.utils]
            [status-im.contexts.profile.settings.visibility-sheet.view :as visibility-sheet]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.re-frame :as rf]))

(defn- on-state-dropdown-press
  []
  (rf/dispatch [:show-bottom-sheet
                {:shell?  true
                 :theme   :dark
                 :content visibility-sheet/view}]))

(defn view
  [{:keys [scroll-y]}]
  (let [app-theme             (rf/sub [:theme])
        {:keys [public-key emoji-hash bio]
         :as   profile}       (rf/sub [:profile/profile-with-image])
        online?               (rf/sub [:visibility-status-updates/online? public-key])
        status                (rf/sub [:visibility-status-updates/visibility-status-update public-key])
        customization-color   (rf/sub [:profile/customization-color])
        full-name             (profile.utils/displayed-name profile)
        profile-picture       (profile.utils/photo profile)
        {:keys [status-title
                status-icon]} (header.utils/visibility-status-type-data status)]
    [:<>
     [header.shape/view
      {:scroll-y            scroll-y
       :customization-color customization-color}]
     [rn/view {:style style/avatar-row-wrapper}
      [avatar/view
       {:scroll-y            scroll-y
        :full-name           full-name
        :online?             online?
        :border-color        (style/avatar-border-color app-theme)
        :customization-color customization-color
        :profile-picture     profile-picture}]
      [quo/dropdown
       {:background     :blur
        :size           :size-32
        :type           :outline
        :icon?          true
        :no-icon-color? true
        :icon-name      status-icon
        :on-press       on-state-dropdown-press}
       status-title]]
     [quo/page-top
      {:title-accessibility-label :username
       :emoji-dash                emoji-hash
       :description               :text
       :description-text          bio
       :title                     full-name}]]))
