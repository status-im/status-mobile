(ns status-im.contexts.settings.wallet.keypairs-and-accounts.remove.view
  (:require [quo.core :as quo]
            [quo.foundations.colors :as colors]
            [react-native.core :as rn]
            [status-im.common.standard-authentication.core :as standard-auth]
            [status-im.contexts.settings.wallet.keypairs-and-accounts.remove.style :as style]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(defn view
  [{:keys [name key-uid]}]
  (let [customization-color (rf/sub [:profile/customization-color])
        on-remove           (rn/use-callback #()
                                             [key-uid])]
    [:<>
     [quo/page-top
      {:container-style style/header-container
       :title           (i18n/label :t/remove-key-pair-and-derived-accounts)
       :title-size      :heading-2
       :description     :context-tag
       :context-tag     {:type    :icon
                         :size    24
                         :context name
                         :icon    :i/seed-phrase}}]
     [rn/view {:style style/content}
      [quo/text
       {:style  {:margin-top 4}
        :weight :regular
        :size   :paragraph-1}
       (i18n/label :t/the-key-pair-and-derived-accounts-will-be-removed)]
      [standard-auth/slide-button
       {:size                :size-48
        :track-text          (i18n/label :t/slide-to-remove-key-pair)
        :container-style     {:margin-top 34}
        :customization-color colors/danger-50
        :on-auth-success     on-remove
        :auth-button-label   (i18n/label :t/confirm)}]]]))

