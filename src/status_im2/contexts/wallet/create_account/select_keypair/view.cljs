(ns status-im2.contexts.wallet.create-account.select-keypair.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im2.contexts.profile.utils :as profile.utils]
    [status-im2.contexts.wallet.create-account.select-keypair.style :as style]
    [utils.address :as utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(def accounts
  [{:account-props {:customization-color :turquoise
                    :size                32
                    :emoji               "\uD83C\uDFB2"
                    :type                :default
                    :name                "Trip to Vegas"
                    :address             "0x0ah...71a"}
    :networks      [{:network-name :ethereum :short-name "eth"}
                    {:network-name :optimism :short-name "opt"}]
    :state         :default
    :action        :none}])

(defn view
  []
  (let [{:keys [public-key compressed-key
                customization-color]} (rf/sub [:profile/profile])
        display-name                  (first (rf/sub [:contacts/contact-two-names-by-identity
                                                      public-key]))
        profile-with-image            (rf/sub [:profile/profile-with-image])
        profile-picture               (profile.utils/photo profile-with-image)]
    [rn/view {:style {:flex 1}}
     [quo/page-nav
      {:icon-name           :i/close
       :on-press            #(rf/dispatch [:navigate-back])
       :accessibility-label :top-bar}]
     [quo/text-combinations
      {:container-style     style/header-container
       :title               (i18n/label :t/keypairs)
       :description         (i18n/label :t/keypairs-description)
       :button-icon         :i/add
       :button-on-press     #(js/alert "not implemented")
       :customization-color customization-color}]
     [quo/keypair
      (merge
       {:customization-color customization-color
        :profile-picture     profile-picture
        :status-indicator    false
        :type                :default-keypair
        :stored              :on-device
        :on-options-press    #(js/alert "Options pressed")
        :action              :selector
        :blur?               false
        :details             {:full-name display-name
                              :address   (utils/get-shortened-compressed-key compressed-key)}
        :accounts            accounts
        :container-style     {:margin-horizontal 20
                              :margin-vertical   8}})]
     [quo/bottom-actions
      {:button-one-label (i18n/label :t/confirm-account-origin)
       :button-one-props {:disabled?           true
                          :customization-color customization-color}
       :container-style  style/bottom-action-container}]]))
