(ns status-im.contexts.keycard.empty.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [status-im.contexts.keycard.migrate.sheets.view :as sheets.migrate]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn create
  []
  [:<>
   [quo/page-nav
    {:icon-name :i/close
     :on-press  events-helper/navigate-back}]
   [quo/page-top
    {:title            (i18n/label :t/keycard-empty)
     :description      :text
     :description-text (i18n/label :t/what-to-do)}]
   [quo/small-option-card
    {:variant             :main
     :title               (i18n/label :t/create-new-profile)
     :subtitle            (i18n/label :t/new-key-pair-keycard)
     :button-label        (i18n/label :t/lets-go)
     :accessibility-label :create-new-profile-keycard
     :container-style     {:margin-horizontal 20 :margin-top 8}
     :image               (resources/get-image :keycard-buy)
     :button-type         :primary
     :on-press            #(rf/dispatch [:keycard/create.get-phrase])}]
   [quo/small-option-card
    {:variant             :icon
     :title               (i18n/label :t/import-recovery-phrase-to-keycard)
     :subtitle            (i18n/label :t/store-key-pair-on-keycard)
     :accessibility-label :import-recovery-phrase-to-keycard
     :container-style     {:margin 20}
     :image               (resources/get-image :use-keycard)
     :on-press            #(rf/dispatch [:open-modal :screen/use-recovery-phrase-dark
                                         {:on-success (fn [{:keys [key-uid phrase]}]
                                                        (rf/dispatch [:keycard/create.seed-phrase-entered
                                                                      key-uid phrase]))}])}]])

(defn view
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
      :accessibility-label :import-key-pair-keycard
      :image               (resources/get-image :keycard-buy)
      :on-press            (fn []
                             (rf/dispatch
                              [:show-bottom-sheet
                               {:theme   :dark
                                :content (fn []
                                           [sheets.migrate/view
                                            {:on-continue #(rf/dispatch
                                                            [:keycard/migration.get-phrase])}])}]))}]]
   [quo/information-box
    {:type  :default
     :style {:margin-top 32 :margin-horizontal 28}}
    (i18n/label :t/empty-card-info)]])
