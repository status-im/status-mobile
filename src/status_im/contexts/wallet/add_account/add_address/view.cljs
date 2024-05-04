(ns status-im.contexts.wallet.add-account.add-address.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.clipboard :as clipboard]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.constants :as constants]
    [status-im.contexts.wallet.add-account.add-address.style :as style]
    [status-im.contexts.wallet.common.validation :as validation]
    [status-im.subs.wallet.add-account.address-to-watch]
    [utils.debounce :as debounce]
    [utils.ens.core :as utils.ens]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- validate-address
  [known-addresses user-input purpose]
  (cond
    (or (nil? user-input) (= user-input ""))       nil
    ;; Allow adding existing address if saving, As it'll upsert
    (and (not= purpose constants/add-address-to-save-type)
         (some #(= % user-input) known-addresses)) (i18n/label :t/address-already-in-use)
    (not
     (or (validation/eth-address? user-input)
         (validation/ens-name? user-input)))       (i18n/label :t/invalid-address)))

(defn- paste-on-input-fn
  [on-change-text]
  (clipboard/get-string
   (fn [clipboard-text]
     (on-change-text clipboard-text))))

(defn- on-press-scan-address
  []
  (rn/dismiss-keyboard!)
  (rf/dispatch [:open-modal :screen/wallet.scan-address]))

(defn- address-input
  [{:keys [input-value validate clear-input set-validation-message set-input-value input-title
           accessibility-label]}]
  (let [scanned-address (rf/sub [:wallet/scanned-address])
        empty-input?    (and (string/blank? input-value)
                             (string/blank? scanned-address))
        on-change-text  (fn [new-text]
                          (set-validation-message (validate new-text))
                          (set-input-value new-text)
                          (reagent/flush)
                          (if (and (not-empty new-text) (nil? (validate new-text)))
                            (debounce/debounce-and-dispatch [:wallet/get-address-details new-text]
                                                            500)
                            (rf/dispatch [:wallet/clear-address-activity]))
                          (when (and scanned-address (not= scanned-address new-text))
                            (rf/dispatch [:wallet/clear-address-activity])
                            (rf/dispatch [:wallet/clean-scanned-address])))
        paste-on-input  #(paste-on-input-fn on-change-text)]
    (rn/use-effect (fn []
                     (when-not (string/blank? scanned-address)
                       (on-change-text scanned-address)))
                   [scanned-address])
    [rn/view {:style style/input-container}
     [quo/input
      {:accessibility-label accessibility-label
       :placeholder         (i18n/label :t/address-placeholder)
       :container-style     style/input
       :label               input-title
       :auto-capitalize     :none
       :multiline?          true
       :on-clear            clear-input
       :return-key-type     :done
       :clearable?          (not empty-input?)
       :on-change-text      on-change-text
       :button              (when empty-input?
                              {:on-press paste-on-input
                               :text     (i18n/label :t/paste)})
       :value               (or scanned-address input-value)}]
     [quo/button
      {:type            :outline
       :on-press        on-press-scan-address
       :container-style style/scan
       :size            40
       :icon-only?      true}
      :i/scan]]))

(defn activity-indicator
  [activity-state]
  (let [{:keys [message]
         :as   props} (case activity-state
                        :has-activity               {:accessibility-label :account-has-activity
                                                     :icon                :i/done
                                                     :type                :success
                                                     :message             :t/address-activity}
                        :no-activity                {:accessibility-label :account-has-no-activity
                                                     :icon :i/info
                                                     :type :warning
                                                     :message :t/this-address-has-no-activity}
                        :invalid-ens                {:accessibility-label :error-message
                                                     :icon                :i/info
                                                     :type                :error
                                                     :message             :t/invalid-address}
                        :address-already-registered {:accessibility-label :error-message
                                                     :icon                :i/info
                                                     :type                :error
                                                     :message             :t/address-already-in-use}
                        {:accessibility-label :searching-for-activity
                         :icon                :i/pending-state
                         :type                :default
                         :message             :t/searching-for-activity})]
    (when activity-state
      [quo/info-message
       (assoc props
              :style style/info-message
              :size  :default)
       (i18n/label message)])))

(defn- on-press-close
  []
  (rf/dispatch [:wallet/clean-scanned-address])
  (rf/dispatch [:wallet/clear-address-activity])
  (rf/dispatch [:navigate-back]))

(defn- clear-activity-and-scanned-address
  []
  (rf/dispatch [:wallet/clear-address-activity])
  (rf/dispatch [:wallet/clean-scanned-address]))

(defn- on-press-confirm-add-address
  [input-value adding-address-purpose]
  (rf/dispatch
   [:wallet/confirm-add-address
    {:address                input-value
     :ens?                   (utils.ens/is-valid-eth-name?
                              input-value)
     :adding-address-purpose adding-address-purpose}]))

(defn view
  []
  (let [addresses (rf/sub [:wallet/lowercased-addresses])
        {:keys [title description input-title adding-address-purpose accessibility-label]}
        (rf/sub [:wallet/currently-added-address])
        validate #(validate-address addresses (string/lower-case %) adding-address-purpose)
        customization-color (rf/sub [:profile/customization-color])]
    (rf/dispatch [:wallet/clean-scanned-address])
    (rf/dispatch [:wallet/clear-address-activity])
    (fn []
      (let [activity-state                          (rf/sub [:wallet/watch-address-activity-state])
            validated-address                       (rf/sub [:wallet/watch-address-validated-address])
            [input-value set-input-value]           (rn/use-state nil)
            [validation-msg set-validation-message] (rn/use-state nil)
            clear-input                             (fn []
                                                      (set-input-value nil)
                                                      (set-validation-message nil)
                                                      (clear-activity-and-scanned-address))]
        [rn/view
         {:style {:flex 1}}
         (when (= constants/add-address-to-save-type adding-address-purpose)
           [quo/drawer-bar])
         [floating-button-page/view
          {:header [quo/page-nav
                    {:type      :no-title
                     :icon-name :i/close
                     :on-press  on-press-close}]
           :footer [quo/button
                    {:customization-color customization-color
                     :disabled?           (or (string/blank? input-value)
                                              (some? (validate input-value))
                                              (= activity-state :invalid-ens)
                                              (= activity-state :scanning)
                                              (not validated-address))
                     :on-press            (fn []
                                            (on-press-confirm-add-address input-value
                                                                          adding-address-purpose)
                                            (clear-input))
                     :container-style     {:z-index 2}}
                    (i18n/label :t/continue)]}
          [quo/page-top
           {:container-style  style/header-container
            :title            (i18n/label title)
            :description      :text
            :description-text (i18n/label description)}]
          [address-input
           {:input-value            input-value
            :validate               validate
            :validation-msg         validation-msg
            :clear-input            clear-input
            :set-validation-message set-validation-message
            :set-input-value        set-input-value
            :input-title            (i18n/label input-title)
            :adding-address-purpose adding-address-purpose
            :accessibility-label    accessibility-label}]
          (if validation-msg
            [quo/info-message
             {:accessibility-label :error-message
              :size                :default
              :icon                :i/info
              :type                :error
              :style               style/info-message}
             validation-msg]
            [activity-indicator activity-state])]]))))
