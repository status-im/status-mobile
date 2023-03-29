(ns status-im2.contexts.onboarding.syncing.syncing-devices.view
  (:require [quo2.core :as quo]
            [utils.i18n :as i18n]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [utils.re-frame :as rf]
            [status-im2.contexts.onboarding.syncing.syncing-devices.style :as style]
            [status-im2.contexts.onboarding.common.background.view :as background]
            [status-im2.contexts.onboarding.common.syncing.render-device :as device]))

(defn page-title
  [local-paring-status]
  [rn/view {:style {:padding-horizontal 20}}
   [quo/text
    {:accessibility-label :notifications-screen-title
     :weight              :semi-bold
     :size                :heading-1
     :style               {:color colors/white}}
    (when (= :connection-success local-paring-status)
      (i18n/label :t/sync-devices-title))
    (when (= :error-on-pairing local-paring-status)
      (i18n/label :t/sync-devices-error-title))
    (when (= :completed-pairing local-paring-status)
      (i18n/label :t/sync-devices-complete-title))]
   [quo/text
    {:accessibility-label :notifications-screen-sub-title
     :weight              :regular
     :size                :paragraph-1
     :style               {:color colors/white}}
    (when (= :connection-success local-paring-status)
      (i18n/label :t/sync-devices-sub-title))
    (when (= :error-on-pairing local-paring-status)
      (i18n/label :t/sync-devices-error-sub-title))
    (when (= :completed-pairing local-paring-status)
      (i18n/label :t/sync-devices-complete-sub-title))]])

(defn navigation-bar
  []
  [rn/view {:style style/navigation-bar}
   [quo/page-nav
    {:align-mid?            true
     :right-section-buttons [{:type                :blur-bg
                              :icon                :i/info
                              :icon-override-theme :dark
                              :on-press            #(js/alert "Pending")}]}]])

(defn render-device-list
  [installations]
  [rn/scroll-view
   {:flex               1
    :margin-top         24
    :padding-horizontal 20}
   [device/render-device
    (merge (first installations)
           {:this-device? true})]
   [rn/view {:style {:flex 1}}
    [quo/text
     {:accessibility-label :notifications-screen-sub-title
      :weight              :regular
      :size                :paragraph-1
      :style               {:color colors/white}}
     (i18n/label :t/sync-with)]
    (when (seq installations)
      [rn/flat-list
       {:data               (rest installations)
        :default-separator? false
        :key-fn             :installation-id
        :render-fn          device/render-device}])]])


(defn view
  []
  (let [local-paring-status (:pairing-status (rf/sub [:get-screen-params]))
        profile-color       (:color (rf/sub [:onboarding-2/profile]))
        installations       (rf/sub [:pairing/installations])]
    [rn/view {:style style/page-container}
     [background/view true]
     [navigation-bar]
     [page-title local-paring-status]
     (if (= :completed-pairing local-paring-status)
       [render-device-list installations]
       [rn/view {:style style/page-illustration}
        (when
          (= :connection-success local-paring-status)
          [quo/text "[Success here]"])
        (when
          (= :error-on-pairing local-paring-status)
          [quo/text "[Error here]"])])
     (when-not (= :connection-success local-paring-status)
       [quo/button
        {:on-press                  (when-not (= :completed-pairing local-paring-status)
                                      #(rf/dispatch [:init-root :enable-notifications])
                                      #(rf/dispatch [:navigate-back]))
         :accessibility-label       :enable-notifications-later-button
         :override-background-color (colors/custom-color profile-color 60)
         :style                     {:margin-top         20
                                     :padding-horizontal 20}}
        (i18n/label (if (= :error-on-pairing local-paring-status)
                      :t/try-again
                      :t/continue))])]))
