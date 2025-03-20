(ns status-im.contexts.keycard.manage.profile-keys.view
  (:require [quo.core :as quo]
            [status-im.common.events-helper :as events-helper]
            [status-im.contexts.keycard.backup.view :as backup.view]
            [status-im.contexts.keycard.change-pin.view :as change-pin.view]
            [status-im.contexts.keycard.factory-reset.view :as factory-reset]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn- backup-sheet
  []
  [backup.view/sheet
   {:on-continue
    (fn []
      (rf/dispatch [:navigate-back])
      (rf/dispatch [:open-modal :screen/keycard.backup.scan-empty]))}])

(defn view
  []
  (let [profile-name (rf/sub [:profile/name])]
    [:<>
     [quo/page-nav
      {:icon-name :i/close
       :on-press  events-helper/navigate-back}]
     [quo/page-top
      {:title profile-name}]
     [quo/keycard]
     [quo/category
      {:list-type :settings
       :label (i18n/label :t/what-you-can-do)
       :blur? true
       :data
       [{:title             (i18n/label :t/change-pin-keycard)
         :image             :icon
         :image-props       :i/pincode
         :action            :arrow
         :description       :text
         :description-props {:text (i18n/label :t/change-pin-keycard-description)}
         :on-press          (fn []
                              (rf/dispatch [:show-bottom-sheet
                                            {:theme   :dark
                                             :shell?  true
                                             :content change-pin.view/change-pin-confirmation-sheet}]))}
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
                                             :content factory-reset/sheet}]))}
        {:title             (i18n/label :t/backup-keycard)
         :image             :icon
         :image-props       :i/copy
         :action            :arrow
         :description       :text
         :description-props {:text (i18n/label :t/copy-keycard)}
         :on-press          (fn []
                              (rf/dispatch [:show-bottom-sheet
                                            {:theme   :dark
                                             :shell?  true
                                             :content backup-sheet}]))}]}]]))
