(ns status-im.contexts.keycard.manage.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [status-im.contexts.keycard.factory-reset.view :as factory-reset]
            [status-im.contexts.keycard.migrate.sheets.view :as sheets.migrate]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn empty-import-view
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title            (i18n/label :t/keycard-empty)
     :description      :text
     :description-text (i18n/label :t/what-to-do)}]
   [rn/view {:style {:padding-horizontal 28 :padding-top 20}}
    [quo/small-option-card
     {:variant             :main
      :title               (i18n/label :t/import-key-pair-keycard)
      :subtitle            (i18n/label :t/use-keycard-login-sign)
      :button-label        (i18n/label :t/import-profile-key-pair)
      :button-props        {:type :primary}
      :accessibility-label :import-key-pair-keycard
      :image               (resources/get-image :keycard-buy)
      :on-press            (fn []
                             (rf/dispatch
                              [:show-bottom-sheet
                               {:theme :dark
                                :content
                                (fn []
                                  [sheets.migrate/view
                                   {:on-continue #(rf/dispatch [:keycard/migration.get-phrase])}])}]))}]]
   [quo/information-box
    {:type  :default
     :style {:margin-top 32 :margin-horizontal 28}}
    (i18n/label :t/empty-card-info)]])

(defn empty-backup-view
  []
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title            (i18n/label :t/keycard-empty)
     :description      :text
     :description-text (i18n/label :t/no-key-pair-keycard)}]
   [quo/keycard]
   [quo/category
    {:list-type :settings
     :label (i18n/label :t/what-you-can-do)
     :blur? true
     :data
     [{:title             (i18n/label :t/use-backup-keycard)
       :image             :icon
       :image-props       :i/copy
       :action            :arrow
       :description       :text
       :description-props {:text (i18n/label :t/create-backup-profile-keycard)}
       :on-press          (fn []
                            (rf/dispatch [:navigate-back])
                            (rf/dispatch [:keycard/backup-on-empty-card]))}]}]])

(defn not-empty-logout-view
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title            (i18n/label :t/keycard-not-empty)
     :description      :text
     :description-text (i18n/label :t/cant-store-new-keys)}]
   [quo/keycard]
   [quo/category
    {:list-type :settings
     :label (i18n/label :t/what-you-can-do)
     :blur? true
     :data
     [{:title             (i18n/label :t/logout-login-keycard)
       :image             :icon
       :image-props       :i/log-out
       :action            :arrow
       :description       :text
       :description-props {:text (i18n/label :t/keycard-has-keypair)}
       :on-press          (fn []
                            (rf/dispatch [:navigate-back])
                            (rf/dispatch [:profile/logout]))}
      {:title             (i18n/label :t/factory-reset)
       :image             :icon
       :image-props       :i/revert
       :action            :arrow
       :description       :text
       :description-props {:text (i18n/label :t/remove-keycard-content)}
       :on-press          (fn []
                            (rf/dispatch [:show-bottom-sheet
                                          {:theme   :dark
                                           :shell?  true
                                           :content factory-reset/sheet}]))}]}]])
