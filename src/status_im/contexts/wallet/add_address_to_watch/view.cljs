(ns status-im.contexts.wallet.add-address-to-watch.view
  (:require
    [clojure.string :as string]
    [quo.core :as quo]
    [react-native.clipboard :as clipboard]
    [react-native.core :as rn]
    [reagent.core :as reagent]
    [status-im.common.floating-button-page.view :as floating-button-page]
    [status-im.contexts.wallet.add-address-to-watch.style :as style]
    [status-im.contexts.wallet.common.validation :as validation]
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
                          (reagent/flush)
                          (if (and (not-empty new-text) (nil? (validate new-text)))
                            (rf/dispatch [:wallet/get-address-details new-text])
                            (rf/dispatch [:wallet/clear-address-activity-check]))
                          (when (and scanned-address (not= scanned-address new-text))
                            (rf/dispatch [:wallet/clear-address-activity-check])
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
       :value               (or scanned-address @input-value)}]
     [quo/button
      {:type            :outline
       :on-press        (fn []
                          (rn/dismiss-keyboard!)
                          (rf/dispatch [:open-modal :scan-address]))
       :container-style style/scan
       :size            40
       :icon-only?      true}
      :i/scan]]))

(defn activity-indicator
  []
  (let [activity-state (rf/sub [:wallet/watch-address-activity-state])
        {:keys [accessibility-label icon type message]}
        (case activity-state
          :has-activity {:accessibility-label :account-has-activity
                         :icon                :i/done
                         :type                :success
                         :message             :t/this-address-has-activity}
          :no-activity  {:accessibility-label :account-has-no-activity
                         :icon                :i/info
                         :type                :warning
                         :message             :t/this-address-has-no-activity}
          {:accessibility-label :searching-for-activity
           :icon                :i/pending-state
           :type                :default
           :message             :t/searching-for-activity})]
    (when activity-state
      [quo/info-message
       {:accessibility-label accessibility-label
        :size                :default
        :icon                icon
        :type                type
        :style               style/info-message}
       (i18n/label message)])))

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
                              (rf/dispatch [:wallet/clear-address-activity-check])
                              (rf/dispatch [:wallet/clean-scanned-address]))
        customization-color (rf/sub [:profile/customization-color])]
    (rf/dispatch [:wallet/clean-scanned-address])
    (rf/dispatch [:wallet/clear-address-activity-check])
    (fn []
      [rn/view
       {:style {:flex 1}}
       [floating-button-page/view
        {:header [quo/page-nav
                  {:type      :no-title
                   :icon-name :i/close
                   :on-press  (fn []
                                (rf/dispatch [:wallet/clean-scanned-address])
                                (rf/dispatch [:wallet/clear-address-activity-check])
                                (rf/dispatch [:navigate-back]))}]
         :footer [quo/button
                  {:customization-color customization-color
                   :disabled?           (or (string/blank? @input-value)
                                            (some? (validate @input-value)))
                   :on-press            (fn []
                                          (rf/dispatch [:navigate-to
                                                        :confirm-address-to-watch
                                                        {:address @input-value}])
                                          (clear-input))
                   :container-style     {:z-index 2}}
                  (i18n/label :t/continue)]}
        [quo/page-top
         {:container-style  style/header-container
          :title            (i18n/label :t/add-address)
          :description      :text
          :description-text (i18n/label :t/enter-eth)}]
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
        [activity-indicator]]])))
