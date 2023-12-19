(ns status-im.contexts.profile.settings.header.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [status-im.common.not-implemented :as not-implemented]
            [status-im.contexts.profile.settings.header.avatar :as header.avatar]
            [status-im.contexts.profile.settings.header.header-shape :as header.shape]
            [status-im.contexts.profile.settings.header.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- f-view
  [{:keys [theme scroll-y]}]
  (let [{:keys [public-key emoji-hash] :as profile} (rf/sub [:profile/profile-with-image])
        online?                                     (rf/sub [:visibility-status-updates/online?
                                                             public-key])
        customization-color                         (rf/sub [:profile/customization-color])
        full-name                                   (profile.utils/displayed-name profile)
        profile-picture                             (profile.utils/photo profile)
        emoji-string                                (string/join emoji-hash)]
    [:<>
     [header.shape/view
      {:scroll-y            scroll-y
       :customization-color customization-color
       :theme               theme}]
     [rn/view {:style style/avatar-row-wrapper}
      [header.avatar/view
       {:scroll-y            scroll-y
        :display-name        full-name
        :online?             online?
        :customization-color customization-color
        :profile-picture     profile-picture}]
      [rn/view {:style {:margin-bottom 4}}
       [quo/dropdown
        {:background :blur
         :size       :size-32
         :type       :outline
         :icon?      true
         :icon-name  :i/online
         :on-press   not-implemented/alert}
        (i18n/label :t/online)]]]
     [quo/text-combinations
      {:container-style style/title-container
       :emoji-hash      emoji-string
       :title           full-name}]]))

(def view (quo.theme/with-theme f-view))
