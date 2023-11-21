(ns status-im2.contexts.wallet.add-address-to-watch.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.clipboard :as clipboard]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im2.contexts.wallet.add-address-to-watch.style :as style]
    [status-im2.contexts.wallet.common.validation :as validation]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn validate-message
  [addresses]
  (fn [s]
    (cond
      (or (= s nil) (= s ""))             nil
      (contains? addresses s)             (i18n/label :t/address-already-in-use)
      (not (or (validation/eth-address? s)
               (validation/ens-name? s))) (i18n/label :t/invalid-address)
      :else                               nil)))

(defn- address-input
  [{:keys [input-value validation-msg validate
           clear-input]}]
  (let [scanned-address (rf/sub [:wallet/scanned-address])
        empty-input?    (and (string/blank? @input-value)
                             (string/blank? scanned-address))

        on-change-text  (fn [new-text]
                          (reset! validation-msg (validate new-text))
                          (reset! input-value new-text)
                          (when (and scanned-address (not= scanned-address new-text))
                            (rf/dispatch [:wallet/clean-scanned-address])))
        paste-on-input  #(clipboard/get-string
                          (fn [clipboard-text]
                            (on-change-text clipboard-text)))]
    (rn/use-effect (fn []
                     (when-not (string/blank? scanned-address)
                       (on-change-text scanned-address)))
                   [scanned-address])
    [rn/view
     {:style style/input-container}
     [quo/input
      {:accessibility-label :add-address-to-watch
       :placeholder         (i18n/label :t/address-placeholder)
       :container-style     style/input
       :label               (i18n/label :t/eth-or-ens)
       :auto-capitalize     :none
       :multiline?          true
       :on-clear            clear-input
       :return-key-type     :done
       :clearable?          (not empty-input?)
       :on-change-text      on-change-text
       :button              (when empty-input?
                              {:on-press paste-on-input
                               :text     (i18n/label :t/paste)})
       :value               @input-value}]
     [quo/button
      {:type            :outline
       :on-press        (fn []
                          (rn/dismiss-keyboard!)
                          (rf/dispatch [:open-modal :scan-address]))
       :container-style style/scan
       :size            40
       :icon-only?      true}
      :i/scan]]))

(defn view
  []
  (let [addresses           (rf/sub [:wallet/addresses])
        input-value         (reagent/atom nil)
        validate            (validate-message (set addresses))
        validation-msg      (reagent/atom (validate
                                           @input-value))
        clear-input         (fn []
                              (reset! input-value nil)
                              (reset! validation-msg nil)
                              (rf/dispatch [:wallet/clean-scanned-address]))
        customization-color (rf/sub [:profile/customization-color])]
    (rf/dispatch [:wallet/clean-scanned-address])
    (fn []
      [rn/view
       {:style {:flex 1}}
       [quo/page-nav
        {:type      :no-title
         :icon-name :i/close
         :on-press  (fn []
                      (rf/dispatch [:wallet/clean-scanned-address])
                      (rf/dispatch [:navigate-back]))}]
       [quo/text-combinations
        {:container-style style/header-container
         :title           (i18n/label :t/add-address)
         :description     (i18n/label :t/enter-eth)}]
       [:f> address-input
        {:input-value    input-value
         :validate       validate
         :validation-msg validation-msg
         :clear-input    clear-input}]
       (when @validation-msg
         [quo/info-message
          {:accessibility-label :error-message
           :size                :default
           :icon                :i/info
           :type                :error
           :style               style/info-message}
          @validation-msg])
       [quo/button
        {:customization-color customization-color
         :disabled?           (string/blank? @input-value)
         :on-press            #(rf/dispatch [:navigate-to
                                             :confirm-address-to-watch
                                             {:address @input-value}])
         :container-style     style/button-container}
        (i18n/label :t/continue)]])))
