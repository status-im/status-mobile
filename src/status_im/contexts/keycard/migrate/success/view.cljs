(ns status-im.contexts.keycard.migrate.success.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.resources :as resources]
            [status-im.constants :as constants]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [profile-name        (rf/sub [:profile/name])
        profile-picture     (rf/sub [:profile/image])
        customization-color (rf/sub [:profile/customization-color])]
    [:<>
     [quo/page-top
      {:title           (i18n/label :t/key-pair-migrated-successfully)
       :description     :context-tag
       :context-tag     {:full-name           profile-name
                         :profile-picture     profile-picture
                         :customization-color customization-color}
       :container-style {:margin-top constants/page-nav-height}}]
     [quo/text {:style {:padding-horizontal 20}}
      (i18n/label :t/use-keycard-for-status)]
     [rn/view {:style {:flex 1 :align-items :center :justify-content :center}}
      [rn/image
       {:resize-mode :contain
        :source      (resources/get-image :keycard-migration-succeeded)}]]
     [quo/button
      {:on-press        #(rf/dispatch [:logout])
       :container-style {:margin-horizontal 20}}
      (i18n/label :t/logout)]]))
