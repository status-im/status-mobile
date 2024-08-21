(ns status-im.contexts.profile.settings.screens.syncing.view
  (:require
    [quo.core :as quo]
    [status-im.common.data-confirmation-sheet.view :as data-confirmation-sheet]
    [status-im.common.events-helper :as events-helper]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- open-paired-devices-list
  []
  (rf/dispatch [:open-modal :screen/paired-devices]))

(defn- open-data-confirmation-sheet
  []
  (rf/dispatch
   [:show-bottom-sheet
    {:content (fn [] [data-confirmation-sheet/view])
     :shell?  true
     :theme   :dark}]))

(defn view
  []
  (let [pairing-devices-count      (rf/sub [:pairing/paired-devices-count])
        syncing-on-mobile-network? (rf/sub [:profile/syncing-on-mobile-network?])]
    [quo/overlay {:type :shell :top-inset? true}
     [quo/page-nav
      {:background :blur
       :icon-name  :i/arrow-left
       :on-press   events-helper/navigate-back}]
     [quo/page-top {:title (i18n/label :t/syncing)}]
     [quo/category
      {:data      [{:title             (i18n/label :t/sync-and-backup)
                    :description       :text
                    :description-props {:text (if syncing-on-mobile-network?
                                                (i18n/label :t/wifi-and-mobile-data)
                                                (i18n/label :t/mobile-network-use-wifi))}
                    :on-press          open-data-confirmation-sheet
                    :blur?             true
                    :image-props       :i/syncing
                    :image             :icon
                    :action            :arrow}
                   {:title             (i18n/label :t/paired-devices)
                    :description       :text
                    :description-props {:text (i18n/label :t/devices-count
                                                          {:number pairing-devices-count})}
                    :on-press          open-paired-devices-list
                    :image-props       :i/mobile
                    :image             :icon
                    :blur?             true
                    :action            :arrow}]
       :blur?     true
       :list-type :settings}]]))
