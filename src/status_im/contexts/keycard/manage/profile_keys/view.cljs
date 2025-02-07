(ns status-im.contexts.keycard.manage.profile-keys.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.contexts.keycard.backup.view :as backup.view]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- backup-sheet
  []
  [backup.view/sheet
   {:on-continue
    (fn []
      (rf/dispatch [:navigate-back])
      (rf/dispatch
       [:open-modal :screen/keycard.backup.scan-empty]))}])

(defn view
  []
  (let [profile-name (rf/sub [:profile/name])]
    [:<>
     [quo/page-nav
      {:icon-name :i/close
       :on-press  events-helper/navigate-back}]
     [quo/page-top
      {:title profile-name}]
     [rn/view {:style {:margin-horizontal 20}}
      [rn/view {:style {:padding-top 8 :padding-bottom 20}}
       [quo/keycard {:holder-name ""}]]
      [quo/section-label
       {:section (i18n/label :t/what-you-can-do) :container-style {:padding-vertical 8}}]
      [quo/settings-item
       {:title             (i18n/label :t/backup-keycard)
        :image             :icon
        :image-props       :i/profile
        :action            :arrow
        :description       :text
        :description-props {:text (i18n/label :t/create-backup-profile-keycard)}
        :on-press          (fn []
                             (rf/dispatch [:show-bottom-sheet {:content backup-sheet}]))}]]]))
