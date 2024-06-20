(ns status-im.contexts.wallet.sheets.missing-keypair.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.sheets.missing-keypair.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn view
  [{:keys [name emoji color type] :as _account} keypair]
  (let [customization-color (rf/sub [:profile/customization-color])]
    [:<>
     [quo/drawer-top
      {:title               (i18n/label :t/import-keypair-to-use-account)
       :type                :context-tag
       :context-tag-type    :account
       :account-name        name
       :emoji               emoji
       :customization-color color}]
     [rn/view {:style style/description-container}
      [quo/text {:weight :medium}
       (i18n/label :t/import-keypair-steps
                   {:account-name name
                    :keypair-name (:name keypair)})]]
     [quo/bottom-actions
      {:actions          :two-actions
       :button-one-label (i18n/label :t/import-key-pair)
       :button-one-props {:on-press
                          (fn []
                            (case type
                              :seed
                              (rf/dispatch [:navigate-to
                                            :screen/settings.missing-keypair.import-seed-phrase keypair])
                              :key
                              (rf/dispatch [:navigate-to
                                            :screen/settings.missing-keypair-import-private-key keypair])
                              nil))
                          :customization-color customization-color}
       :button-two-label (i18n/label :t/not-now)
       :button-two-props {:on-press #(rf/dispatch [:hide-bottom-sheet])
                          :type     :grey}}]]))
