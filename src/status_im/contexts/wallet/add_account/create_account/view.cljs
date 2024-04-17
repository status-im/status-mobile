(ns status-im.contexts.wallet.add-account.create-account.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [quo.foundations.colors :as colors]
    [quo.theme :as quo.theme]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [reagent.core :as reagent]
    [status-im.common.emoji-picker.utils :as emoji-picker.utils]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.common.standard-authentication.core :as standard-auth]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.add-account.create-account.style :as style]
    [status-im.contexts.wallet.add-account.create-account.utils :as create-account.utils]
    [status-im.contexts.wallet.common.utils :as utils]
    [status-im.contexts.wallet.sheets.account-origin.view :as account-origin]
    [status-im.feature-flags :as ff]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.responsiveness :as responsiveness]
    [utils.security.core :as security]
    [utils.string]))

(defn- get-keypair-data
  [{:keys [title primary-keypair? new-keypair? derivation-path customization-color]}]
  (let [formatted-path  (string/replace derivation-path #"/" " / ")
        on-auth-success (fn [password]
                          (rf/dispatch [:navigate-to
                                        :screen/wallet.edit-derivation-path
                                        {:password                password
                                         :current-derivation-path formatted-path}]))]
    [{:title             title
      :image             (if primary-keypair? :avatar :icon)
      :image-props       (if primary-keypair?
                           {:full-name           (utils.string/get-initials title 1)
                            :size                :xxs
                            :customization-color customization-color}
                           :i/seed)
      :action            (when-not new-keypair? :button)
      :action-props      {:on-press    #(rf/dispatch [:navigate-to :screen/wallet.select-keypair])
                          :button-text (i18n/label :t/edit)
                          :alignment   :flex-start}
      :description       :text
      :description-props {:text (i18n/label :t/on-device)}}
     {:title             (i18n/label :t/derivation-path)
      :image             :icon
      :image-props       :i/derivated-path
      :action            :button
      :action-props      {:on-press    #(if (ff/enabled? ::ff/wallet.edit-derivation-path)
                                          (rf/dispatch [:standard-auth/authorize
                                                        {:on-auth-success   on-auth-success
                                                         :auth-button-label (i18n/label :t/continue)}])
                                          (js/alert "Coming soon!"))
                          :button-text (i18n/label :t/edit)
                          :icon-left   :i/face-id
                          :alignment   :flex-start}
      :description       :text
      :description-props {:text formatted-path}}]))

(defn- f-view
  [_]
  (let [top                   (safe-area/get-top)
        bottom                (safe-area/get-bottom)
        {window-width :width} (rn/get-window)
        account-color         (reagent/atom (rand-nth colors/account-colors))
        emoji                 (reagent/atom (emoji-picker.utils/random-emoji))
        account-name          (reagent/atom "")
        on-change-text        #(reset! account-name %)
        show-account-origin   #(rf/dispatch [:show-bottom-sheet
                                             {:content account-origin/view}])]
    (fn [{:keys [theme]}]
      (let [number-of-accounts                    (count (rf/sub
                                                          [:wallet/accounts-without-watched-accounts]))
            {:keys [address customization-color]} (rf/sub [:profile/profile])
            {:keys [new-keypair]}                 (rf/sub [:wallet/create-account])
            keypairs                              (rf/sub [:wallet/keypairs])
            selected-keypair-uid                  (rf/sub [:wallet/selected-keypair-uid])
            placeholder                           (i18n/label :t/default-account-placeholder)
            derivation-path                       (utils/get-derivation-path
                                                   number-of-accounts)
            keypair                               (some #(when (= (:key-uid %) selected-keypair-uid)
                                                           %)
                                                        keypairs)
            primary-keypair?                      (= selected-keypair-uid (:key-uid (first keypairs)))
            create-new-keypair-account            #(rf/dispatch
                                                    [:wallet/add-keypair-and-create-account
                                                     {:sha3-pwd (security/safe-unmask-data %)
                                                      :new-keypair
                                                      (create-account.utils/prepare-new-keypair
                                                       {:new-keypair new-keypair
                                                        :address address
                                                        :account-name @account-name
                                                        :account-color @account-color
                                                        :emoji @emoji
                                                        :derivation-path
                                                        derivation-path})}])
            create-existing-keypair-account       #(rf/dispatch [:wallet/derive-address-and-add-account
                                                                 {:sha3-pwd (security/safe-unmask-data %)
                                                                  :emoji @emoji
                                                                  :color @account-color
                                                                  :path derivation-path
                                                                  :account-name @account-name}])
            keypair-title                         (or (:keypair-name new-keypair)
                                                      (if primary-keypair?
                                                        (i18n/label :t/keypair-title
                                                                    {:name (:name keypair)})
                                                        (:name keypair)))]
        (rn/use-unmount #(rf/dispatch [:wallet/clear-new-keypair]))
        [floating-button-page/view
         {:gradient-cover?          true
          :footer-container-padding 0
          :header-container-style   {:padding-top top}
          :customization-color      @account-color
          :header                   [quo/page-nav
                                     {:type       :no-title
                                      :background :blur
                                      :right-side [{:icon-name :i/info
                                                    :on-press  show-account-origin}]
                                      :icon-name  :i/close
                                      :on-press   #(rf/dispatch [:navigate-back])}]
          :footer                   [standard-auth/slide-button
                                     {:size                :size-48
                                      :track-text          (i18n/label :t/slide-to-create-account)
                                      :customization-color @account-color
                                      :on-auth-success     (fn [password]
                                                             (if new-keypair
                                                               (create-new-keypair-account password)
                                                               (create-existing-keypair-account
                                                                password)))
                                      :auth-button-label   (i18n/label :t/confirm)
                                      :disabled?           (empty? @account-name)
                                      :container-style     (style/slide-button-container bottom)
                                      :dependencies        [new-keypair]}]}
         [rn/view {:style style/account-avatar-container}
          [quo/account-avatar
           {:customization-color @account-color
            :size                80
            :emoji               @emoji
            :type                :default}]
          [quo/button
           {:size            32
            :type            :grey
            :background      :photo
            :icon-only?      true
            :on-press        #(rf/dispatch [:emoji-picker/open
                                            {:on-select (fn [selected-emoji]
                                                          (reset! emoji selected-emoji))}])
            :container-style style/reaction-button-container}
           :i/reaction]]
         [quo/title-input
          {:customization-color @account-color
           :placeholder         placeholder
           :on-change-text      on-change-text
           :max-length          constants/wallet-account-name-max-length
           :blur?               true
           :disabled?           false
           :default-value       @account-name
           :container-style     style/title-input-container}]
         [quo/divider-line]
         [rn/view {:style style/color-picker-container}
          [quo/text
           {:size   :paragraph-2
            :weight :medium
            :style  (style/color-label theme)}
           (i18n/label :t/colour)]
          [quo/color-picker
           {:default-selected @account-color
            :on-change        #(reset! account-color %)
            :container-style  {:padding-vertical 12
                               :padding-left     (responsiveness/iphone-11-Pro-20-pixel-from-width
                                                  window-width)}}]]
         [quo/divider-line]
         [quo/category
          {:list-type :settings
           :label     (i18n/label :t/origin)
           :data      (get-keypair-data {:title               keypair-title
                                         :primary-keypair?    primary-keypair?
                                         :new-keypair?        (boolean new-keypair)
                                         :derivation-path     derivation-path
                                         :customization-color customization-color})}]]))))

(defn- view-internal
  []
  [:f> f-view])

(def view (quo.theme/with-theme view-internal))
