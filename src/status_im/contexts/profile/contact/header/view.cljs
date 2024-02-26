(ns status-im.contexts.profile.contact.header.view
  (:require [clojure.string :as string]
            [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [quo.theme :as quo.theme]
            [react-native.core :as rn]
            [status-im.common.scalable-avatar.view :as avatar]
            [status-im.contexts.profile.contact.header.style :as style]
            [status-im.contexts.profile.utils :as profile.utils]
            [utils.re-frame :as rf]))

(defn- f-view
  [{:keys [theme scroll-y]}]
  (let [{:keys [public-key customization-color
                emoji-hash]
         :as   profile}     (rf/sub [:contacts/current-contact])
        customization-color (or customization-color :blue)
        full-name           (profile.utils/displayed-name profile)
        bio                 (:bio profile)
        emoji-string        (string/join emoji-hash)
        profile-picture     (profile.utils/photo profile)
        online?             (rf/sub [:visibility-status-updates/online? public-key])]
    [rn/view {:style style/header-container}
     [rn/view {:style style/avatar-wrapper}
      [avatar/view
       {:scroll-y            scroll-y
        :full-name           full-name
        :online?             online?
        :profile-picture     profile-picture
        :border-color        (colors/theme-colors colors/white colors/neutral-95 theme)
        :customization-color customization-color}]]
     [quo/page-top
      {:title            full-name
       :description      :text
       :description-text bio
       :emoji-dash       emoji-string}]]))

(def view (quo.theme/with-theme f-view))
