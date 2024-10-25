(ns status-im.contexts.keycard.sheets.migrate.view
  (:require [quo.core :as quo]
            [react-native.core :as rn]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  []
  (let [profile-name               (rf/sub [:profile/name])
        profile-picture            (rf/sub [:profile/image])
        customization-color        (rf/sub [:profile/customization-color])
        recovery-phrase-backed-up? (rf/sub [:profile/recovery-phrase-backed-up?])]
    [:<>
     [quo/drawer-top
      {:type                :context-tag
       :context-tag-type    :default
       :title               (i18n/label :t/migrate-key-pair-keycard)
       :full-name           profile-name
       :profile-picture     profile-picture
       :customization-color customization-color}]
     [rn/view {:style {:padding-horizontal 20}}
      [quo/text {}
       (i18n/label :t/migrate-key-pair-keycard-default-key {:name profile-name})]
      [quo/information-box
       {:type  :default
        :style {:margin-top 20 :margin-bottom 12}}
       (i18n/label :t/migrate-key-pair-keycard-info)]]
     [quo/bottom-actions
      {:actions          :two-actions
       :button-one-label (i18n/label :t/continue)
       :button-one-props {:on-press #(if recovery-phrase-backed-up?
                                       (rf/dispatch [:open-modal :screen/use-recovery-phrase
                                                     {:on-success (fn [])}])
                                       (rf/dispatch [:open-modal :screen/backup-recovery-phrase
                                                     {:on-success (fn [])}]))}
       :button-two-label (i18n/label :t/cancel)
       :button-two-props {:type     :grey
                          :on-press #(rf/dispatch [:hide-bottom-sheet])}}]]))
