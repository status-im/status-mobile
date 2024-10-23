(ns status-im.contexts.keycard.empty.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [status-im.common.events-helper :as events-helper]
            [status-im.common.resources :as resources]
            [status-im.contexts.keycard.migrate.sheets.view :as sheets.migrate]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

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
      :accessibility-label :get-keycard
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
