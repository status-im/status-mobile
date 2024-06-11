(ns status-im.contexts.settings.wallet.saved-addresses.add-address-to-save.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.clipboard :as clipboard]
    [react-native.core :as rn]
    [react-native.safe-area :as safe-area]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.settings.wallet.saved-addresses.add-address-to-save.style :as style]
    [status-im.contexts.wallet.common.validation :as validation]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn- validate-input
  [account-addresses saved-addresses user-input]
  (cond
    (or (nil? user-input) (= user-input ""))
    nil

    (contains? saved-addresses user-input)
    :existing-saved-address

    (contains? account-addresses user-input)
    :own-account

    (not
     (or (validation/eth-address? user-input)
         (validation/ens-name? user-input)))
    :invalid-address-or-ens))

(defn- address-input
  [{:keys [input-value on-change-text paste-into-input clear-input]}]
  (let [empty-input?    (string/blank? input-value)
        on-scan-address (rn/use-callback #(rf/dispatch [:open-modal :screen/wallet.scan-address
                                                        {:on-result on-change-text}]))]
    [rn/view {:style style/input-container}
     [quo/input
      {:accessibility-label :add-address-to-save
       :placeholder         (i18n/label :t/address-placeholder)
       :container-style     style/input
       :blur?               true
       :label               (i18n/label :t/address-or-ens-name)
       :auto-capitalize     :none
       :multiline?          true
       :on-clear            clear-input
       :return-key-type     :done
       :clearable?          (not empty-input?)
       :on-change-text      on-change-text
       :button              (when empty-input?
                              {:on-press paste-into-input
                               :text     (i18n/label :t/paste)})
       :value               input-value}]
     [quo/button
      {:type            :outline
       :on-press        on-scan-address
       :container-style style/scan-button
       :background      :blur
       :size            40
       :icon-only?      true}
      :i/scan]]))

(defn- error-view
  [{:keys [error]}]
  (let [error-msg (condp = error
                    :existing-saved-address
                    (i18n/label :t/this-address-is-already-saved)

                    :own-account
                    (i18n/label :t/you-cannot-add-your-own-account-as-a-saved-address)

                    :invalid-address-or-ens
                    (i18n/label :t/this-is-not-an-eth-address-or-ens-name)

                    :ens-not-registered
                    (i18n/label :t/this-ens-name-is-not-registered-yet)
                    nil)]
    (when error-msg
      [quo/info-message
       {:accessibility-label :error-message
        :size                :default
        :icon                :i/info
        :type                :error
        :style               style/info-message}
       error-msg])))

(defn- existing-saved-address
  [{:keys [address]}]
  (let [{:keys [name customization-color chain-short-names ens has-ens?]}
        (rf/sub [:wallet/saved-address-by-address
                 address])]
    [rn/view {:style style/existing-saved-address-container}
     [quo/text
      {:size   :paragraph-1
       :weight :medium
       :style  style/existing-saved-address-text}
      (i18n/label :t/existing-saved-address)]
     [quo/saved-address
      {:blur?           true
       :active-state?   true
       :user-props      {:name                name
                         :address             (str chain-short-names address)
                         :ens                 (when has-ens? ens)
                         :customization-color customization-color
                         :blur?               true}
       :container-style style/saved-address-item}]]))

(defn view
  []
  (let [profile-color                 (rf/sub [:profile/customization-color])
        accounts-addresses            (rf/sub [:wallet/addresses])
        saved-addresses-addresses     (rf/sub [:wallet/saved-addresses-addresses])
        [input-value set-input-value] (rn/use-state nil)
        [error set-error]             (rn/use-state nil)
        error?                        (some? error)
        validate                      #(validate-input accounts-addresses
                                                       saved-addresses-addresses
                                                       %)
        clear-input                   (rn/use-callback
                                       (fn []
                                         (set-input-value nil)
                                         (set-error nil)))
        on-change-text                (rn/use-callback
                                       (fn [new-value]
                                         (let [lowercase-value (string/lower-case new-value)]
                                           (set-error (validate lowercase-value))
                                           (set-input-value lowercase-value))))
        paste-into-input              (rn/use-callback #(clipboard/get-string
                                                         (fn [clipboard-text]
                                                           (on-change-text clipboard-text))))
        on-press-continue             (rn/use-callback
                                       (fn []
                                         (rf/dispatch [:wallet/set-address-to-save
                                                       {:address input-value}])
                                         (rf/dispatch
                                          [:navigate-to-within-stack
                                           [:screen/settings.save-address
                                            :screen/settings.add-address-to-save]]))
                                       [input-value])]
    (rn/use-unmount #(rf/dispatch [:wallet/clear-address-to-save]))
    [quo/overlay {:type :shell}
     [floating-button-page/view
      {:footer-container-padding 0
       :header                   [quo/page-nav
                                  {:type                :no-title
                                   :icon-name           :i/close
                                   :behind-overlay?     true
                                   :on-press            navigate-back
                                   :margin-top          (safe-area/get-top)
                                   :accessibility-label :add-address-to-save-page-nav}]
       :footer                   [quo/button
                                  {:customization-color profile-color
                                   :disabled?           (or (string/blank? input-value)
                                                            error?)
                                   :on-press            on-press-continue}
                                  (i18n/label :t/continue)]}
      [quo/page-top
       {:container-style  style/header-container
        :blur?            true
        :title            (i18n/label :t/add-address)
        :description      :text
        :description-text (i18n/label :t/add-address-to-save-description)}]
      [address-input
       {:input-value      input-value
        :on-change-text   on-change-text
        :paste-into-input paste-into-input
        :clear-input      clear-input}]
      (when error?
        [:<>
         [error-view {:error error}]
         (when (= error :existing-saved-address)
           [existing-saved-address {:address input-value}])])]]))
