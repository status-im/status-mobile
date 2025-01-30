(ns status-im.contexts.keycard.migrate.profile-keys.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title            (i18n/label :t/keycard-contains-key-pair)
     :description      :text
     :description-text (i18n/label :t/finalise-migration-instructions)}]
   [rn/view {:style {:margin-horizontal 20}}
    [rn/view {:style {:padding-top 8 :padding-bottom 20}}
     [quo/keycard {:holder-name ""}]]
    [quo/section-label {:section (i18n/label :t/what-you-can-do) :container-style {:padding-vertical 8}}]
    [quo/settings-item
     {:title             (i18n/label :t/finalise-migration)
      :image             :icon
      :image-props       :i/profile
      :action            :arrow
      :description       :text
      :description-props {:text (i18n/label :t/use-keycard-instead-password)}
      :on-press          (fn []
                           (rf/dispatch [:navigate-back])
                           (rf/dispatch [:open-modal :screen/keycard.authorise
                                         {:on-success
                                          #(rf/dispatch [:keycard/migration.authorisation-success
                                                         %])}]))}]]])
