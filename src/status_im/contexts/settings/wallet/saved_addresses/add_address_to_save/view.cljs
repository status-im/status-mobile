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
    [utils.debounce :as debounce]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- navigate-back
  []
  (rf/dispatch [:navigate-back]))

(defn- validate-input
  [account-addresses saved-addresses user-input]
  (cond
    (string/blank? user-input)
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
        on-scan-address (rn/use-callback (fn []
                                           (rf/dispatch [:wallet/clean-scanned-address])
                                           (rf/dispatch [:open-modal :screen/wallet.scan-address
                                                         {:on-result on-change-text}])))]
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
       :auto-focus          true
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
        :icon                :i/alert
        :status              :error
        :container-style     style/info-message}
       error-msg])))

(defn- existing-saved-address
  [{:keys [address]}]
  (let [{:keys [name customization-color chain-short-names ens ens?]}
        (rf/sub [:wallet/saved-address-by-address address])]
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
                         :ens                 (when ens? ens)
                         :customization-color customization-color
                         :blur?               true}
       :container-style style/saved-address-item}]]))

(defn view
  []
  (let [profile-color                       (rf/sub [:profile/customization-color])
        accounts-addresses                  (rf/sub [:wallet/addresses])
        saved-addresses-addresses           (rf/sub [:wallet/saved-addresses-addresses])
        [address-or-ens set-address-or-ens] (rn/use-state "")
        [ens-address set-ens-address]       (rn/use-state "")
        [error set-error]                   (rn/use-state nil)
        error?                              (some? error)
        ens-name?                           (validation/ens-name? address-or-ens)
        address                             (if ens-name? ens-address address-or-ens)
        button-disabled?                    (or (string/blank? address-or-ens)
                                                (and ens-name? (string/blank? ens-address))
                                                error?)
        validate                            #(validate-input accounts-addresses
                                                             saved-addresses-addresses
                                                             %)
        clear-input                         (rn/use-callback
                                             (fn []
                                               (set-address-or-ens "")
                                               (set-ens-address "")
                                               (set-error nil)))
        on-ens-resolve                      (rn/use-callback
                                             (fn [resolved-address]
                                               (set-error (validate resolved-address))
                                               (set-ens-address resolved-address)))
        on-change-text                      (rn/use-callback
                                             (fn [new-value]
                                               (let [trimmed-value (string/trim new-value)]
                                                 (set-error (validate (string/lower-case trimmed-value)))
                                                 (set-address-or-ens trimmed-value)
                                                 (set-ens-address "")
                                                 (when (validation/ens-name? trimmed-value)
                                                   (debounce/debounce-and-dispatch
                                                    [:wallet/resolve-ens
                                                     {:ens        new-value
                                                      :on-success on-ens-resolve
                                                      :on-error   #(set-error :ens-not-registered)}]
                                                    300)))))
        paste-into-input                    (rn/use-callback #(clipboard/get-string
                                                               (fn [clipboard-text]
                                                                 (on-change-text clipboard-text))))
        on-press-continue                   (rn/use-callback
                                             (fn []
                                               (rf/dispatch
                                                [:wallet/set-address-to-save
                                                 {:address address
                                                  :ens     (when ens-name? address-or-ens)
                                                  :ens?    ens-name?}])
                                               (rf/dispatch
                                                [:open-modal :screen/settings.save-address]))
                                             [address ens-name? address-or-ens])]
    (rn/use-mount (fn []
                    (rf/dispatch [:wallet/clean-scanned-address])
                    (rf/dispatch [:wallet/clear-address-to-save])))
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
                                   :disabled?           button-disabled?
                                   :on-press            on-press-continue}
                                  (i18n/label :t/continue)]}
      [quo/page-top
       {:container-style  style/header-container
        :blur?            true
        :title            (i18n/label :t/add-address)
        :description      :text
        :description-text (i18n/label :t/add-address-to-save-description)}]
      [address-input
       {:input-value      address-or-ens
        :on-change-text   on-change-text
        :paste-into-input paste-into-input
        :clear-input      clear-input}]
      (when error?
        [:<>
         [error-view {:error error}]
         (when (= error :existing-saved-address)
           [existing-saved-address {:address address}])])]]))
