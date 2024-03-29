(ns status-im.contexts.wallet.create-account.new-keypair.keypair-name.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.wallet.create-account.new-keypair.keypair-name.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def keypair-name-max-length 15)

(defn view
  []
  (let [keypair-name (reagent/atom "")]
    (fn []
      (let [customization-color (rf/sub [:profile/customization-color])]
        [rn/view {:style {:flex 1}}
         [floating-button-page/view
          {:header [quo/page-nav
                    {:icon-name           :i/arrow-left
                     :on-press            #(rf/dispatch [:navigate-back])
                     :accessibility-label :top-bar}]
           :footer [quo/bottom-actions
                    {:actions          :one-action
                     :button-one-label (i18n/label :t/continue)
                     :button-one-props {:disabled?           (or (zero? (count @keypair-name))
                                                                 (> (count @keypair-name)
                                                                    keypair-name-max-length))
                                        :customization-color customization-color
                                        :on-press            #(rf/dispatch [:wallet/new-keypair-continue
                                                                            {:keypair-name
                                                                             @keypair-name}])}
                     :container-style  style/bottom-action}]}
          [quo/text-combinations
           {:container-style style/header-container
            :title           (i18n/label :t/keypair-name)
            :description     (i18n/label :t/keypair-name-description)}]
          [quo/input
           {:container-style {:margin-horizontal 20}
            :placeholder     (i18n/label :t/keypair-name-input-placeholder)
            :label           (i18n/label :t/keypair-name)
            :char-limit      keypair-name-max-length
            :auto-focus      true
            :on-change-text  #(reset! keypair-name %)}]]]))))
