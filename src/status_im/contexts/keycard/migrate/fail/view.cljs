(ns status-im.contexts.keycard.migrate.fail.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.resources :as resources]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [profile-name        (rf/sub [:profile/name])
        profile-picture     (rf/sub [:profile/image])
        customization-color (rf/sub [:profile/customization-color])]
    [:<>
     [quo/page-top
      {:title           (i18n/label :t/failed-to-migrate-key-pair)
       :description     :context-tag
       :context-tag     {:full-name           profile-name
                         :profile-picture     profile-picture
                         :customization-color customization-color}
       :container-style {:margin-top 56}}]
     [rn/view {:style {:flex 1 :align-items :center :justify-content :center}}
      [rn/image
       {:resize-mode :contain
        :source      (resources/get-image :keycard-migration-failed)}]]
     [quo/divider-label (i18n/label :t/what-you-can-do)]
     [rn/view {:style {:padding-horizontal 10}}
      [quo/markdown-list
       {:container-style {:padding-vertical 10}
        :description     (i18n/label :t/log-out-remove-profile)}]
      [quo/markdown-list
       {:container-style {:padding-bottom 25}
        :description     (i18n/label :t/recover-status-profile)}]]
     [quo/button
      {:on-press        #(rf/dispatch [:logout])
       :container-style {:margin-horizontal 20}}
      (i18n/label :t/log-out-remove)]]))
