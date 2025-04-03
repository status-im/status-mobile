(ns status-im.contexts.wallet.common.transaction-settings.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [status-im.common.controlled-input.utils :as controlled-input]
    [status-im.contexts.wallet.common.utils :as utils]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn custom-settings-sheet
  [_]
  (let [max-base-fee   (rf/sub [:wallet/tx-settings-max-base-fee])
        priority-fee   (rf/sub [:wallet/tx-settings-priority-fee])
        max-gas-amount (rf/sub [:wallet/tx-settings-gas-amount])
        nonce          (rf/sub [:wallet/tx-settings-nonce])
        account-color  (rf/sub [:wallet/current-viewing-account-color])
        current-screen (rf/sub [:view-id])]
    [rn/view
     [quo/drawer-top
      {:title (i18n/label :t/custom)}]
     [quo/category
      {:list-type :settings
       :data      [{:title             (i18n/label :t/max-base-fee)
                    :description-props {:text (str max-base-fee " GWEI")}
                    :image             :none
                    :description       :text
                    :action            :arrow
                    :on-press          #(rf/dispatch [:navigate-to-within-stack
                                                      [:screen/wallet.tx-settings-max-fee
                                                       current-screen]])
                    :label             :text
                    :preview-size      :size-32}
                   {:title             (i18n/label :t/priority-fee)
                    :description-props {:text (str priority-fee " GWEI")}
                    :image             :none
                    :description       :text
                    :action            :arrow
                    :on-press          #(rf/dispatch [:navigate-to-within-stack
                                                      [:screen/wallet.tx-settings-priority-fee
                                                       current-screen]])
                    :label             :text
                    :preview-size      :size-32}
                   {:title             (i18n/label :t/max-gas-amount)
                    :description-props {:text (str max-gas-amount " UNITS")}
                    :image             :none
                    :description       :text
                    :action            :arrow
                    :on-press          #(rf/dispatch [:navigate-to-within-stack
                                                      [:screen/wallet.tx-settings-gas-amount
                                                       current-screen]])
                    :label             :text
                    :preview-size      :size-32}
                   {:title             (i18n/label :t/nonce)
                    :description-props {:text nonce}
                    :image             :none
                    :description       :text
                    :action            :arrow
                    :on-press          #(rf/dispatch [:navigate-to-within-stack
                                                      [:screen/wallet.tx-settings-nonce
                                                       current-screen]])
                    :label             :text
                    :preview-size      :size-32}]}]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-props {:on-press            (fn []
                                                 (rf/dispatch
                                                  [:wallet/custom-transaction-settings-confirmed])
                                                 (rf/dispatch [:hide-bottom-sheet]))
                          :customization-color account-color}
       :button-one-label (i18n/label :t/confirm)}]]))

(defn- estimated-time
  [fees-by-mode fee-mode]
  (-> (get fees-by-mode fee-mode)
      :estimated-time
      utils/estimated-time-v2-format))

(defn- format-title
  [estimated-time-s fee-mode]
  (-> (condp =
        fee-mode
        :tx-fee-mode/normal
        (i18n/label :t/normal)

        :tx-fee-mode/fast
        (i18n/label :t/fast)

        :tx-fee-mode/urgent
        (i18n/label :t/urgent)

        "")
      (str " " estimated-time-s "s")))

(defn settings-sheet
  []
  (let [current-transaction-setting                   (rf/sub [:wallet/tx-settings-fee-mode])
        fees-by-mode                                  (rf/sub [:wallet/suggested-gas-fees-for-setting])
        account-color                                 (rf/sub [:wallet/current-viewing-account-color])
        [transaction-setting set-transaction-setting] (rn/use-state current-transaction-setting)
        set-normal                                    #(set-transaction-setting :tx-fee-mode/normal)
        set-fast                                      #(set-transaction-setting :tx-fee-mode/fast)
        set-urgent                                    #(set-transaction-setting :tx-fee-mode/urgent)
        title                                         (fn [fee-mode]
                                                        (-> fees-by-mode
                                                            (estimated-time fee-mode)
                                                            (format-title fee-mode)))
        set-custom                                    (fn []
                                                        (rf/dispatch
                                                         [:show-bottom-sheet
                                                          {:content custom-settings-sheet}]))]
    [rn/view
     [quo/drawer-top
      {:title (i18n/label :t/transaction-settings)}]
     [quo/category
      {:list-type :settings
       :data
       [{:title             (title :tx-fee-mode/normal)
         :image-props       "🍿"
         :description-props {:text (rf/sub [:wallet/wallet-transaction-setting-fiat-formatted
                                            :tx-fee-mode/normal])}
         :image             :emoji
         :description       :text
         :action            :selector
         :action-props      {:type                :radio
                             :checked?            (= :tx-fee-mode/normal transaction-setting)
                             :customization-color account-color
                             ;; there is an UI isssue in quo/category, it has :on-press event
                             ;; and child radio button has own :on-change. If they are not set
                             ;; to the same action then we are getting inconsistent behaviour
                             ;; when user can click on settings item but cant on radio itself.
                             ;; So duplication is to prevent that until general fix is applied
                             ;; to quo/category
                             :on-change           set-normal}
         :on-press          set-normal
         :label             :text
         :preview-size      :size-32}
        {:title             (title :tx-fee-mode/fast)
         :image-props       "🚗"
         :description-props {:text (rf/sub [:wallet/wallet-transaction-setting-fiat-formatted
                                            :tx-fee-mode/fast])}
         :image             :emoji
         :description       :text
         :action            :selector
         :action-props      {:type                :radio
                             :checked?            (= :tx-fee-mode/fast transaction-setting)
                             :on-change           set-fast
                             :customization-color account-color}
         :on-press          set-fast
         :label             :text
         :preview-size      :size-32}
        {:title             (title :tx-fee-mode/urgent)
         :image-props       "🚀"
         :description-props {:text (rf/sub [:wallet/wallet-transaction-setting-fiat-formatted
                                            :tx-fee-mode/urgent])}
         :image             :emoji
         :description       :text
         :action            :selector
         :action-props      {:type                :radio
                             :checked?            (= :tx-fee-mode/urgent transaction-setting)
                             :customization-color account-color
                             :on-change           set-urgent}
         :on-press          set-urgent
         :label             :text
         :preview-size      :size-32}
        {:title             (i18n/label :t/custom)
         :image-props       :i/edit
         :description-props {:text "Set your own fees and nonce"}
         :image             :icon
         :description       :text
         :action            :selector
         :action-props      {:type                :radio
                             :checked?            (= :tx-fee-mode/custom transaction-setting)
                             :customization-color account-color
                             :on-change           set-custom}
         :on-press          set-custom
         :label             :text
         :preview-size      :size-32}]}]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-props {:on-press            (fn []
                                                 (rf/dispatch [:wallet/quick-fee-mode-confirmed
                                                               transaction-setting])
                                                 (rf/dispatch [:hide-bottom-sheet]))
                          :customization-color account-color}
       :button-one-label (i18n/label :t/confirm)}]]))

(defn- hint
  [{:keys [status text]}]
  [quo/network-tags
   {:title  text
    :status status}])

(defn info-sheet
  [info-title info-content]
  [quo/documentation-drawers
   {:title  info-title
    :shell? true}
   [rn/view
    [quo/text {:size :paragraph-2} info-content]
    [quo/button
     {:type            :outline
      :size            24
      :icon-right      :i/info
      :container-style {:padding-top     21
                        :padding-bottom  (if platform/ios? 14 24)
                        :align-self      :flex-start
                        :justify-content :center}
      :on-press        #(rn/open-url "https://status.app/help/wallet/set-transaction-fees-and-nonce")}
     (i18n/label :t/read-more)]]])

(defn custom-setting-screen
  "conditions-fn parameter should be a function that returns map with keys :status and :hint-text"
  [{:keys [screen-title token-symbol conditions-fn current info-title info-content
           on-save
           with-decimals?]
    :or   {with-decimals? true}}]
  (let [[input-state set-input-state] (rn/use-state (controlled-input/set-value-numeric
                                                     controlled-input/init-state
                                                     current))
        input-value                   (controlled-input/input-value input-state)
        condition                     (when conditions-fn
                                        (conditions-fn input-value))]
    [rn/view
     {:style {:flex 1}}
     [quo/page-nav
      {:type       :title
       :title      screen-title
       :text-align :center
       :right-side [{:icon-name :i/info
                     :on-press  #(rf/dispatch [:show-bottom-sheet
                                               {:content (fn [] [info-sheet info-title
                                                                 info-content])}])}]
       :icon-name  :i/arrow-left
       :on-press   (fn []
                     (rf/dispatch [:navigate-back])
                     (rf/dispatch [:show-bottom-sheet
                                   {:content custom-settings-sheet}]))}]
     [quo/token-input
      {:token-symbol     token-symbol
       :swappable?       false
       :show-token-icon? false
       :value            input-value
       :error?           (= (:status condition) :error)
       :currency-symbol  token-symbol
       :hint-component   [hint
                          {:status (:status condition)
                           :text   (:hint-text condition)}]}]
     [rn/view {:style {:flex 1}}]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-label (i18n/label :t/save-changes)
       :button-one-props {:disabled? (= (:status condition) :error)
                          :on-press  #(on-save (controlled-input/value-numeric input-state))}}]
     [quo/numbered-keyboard
      {:container-style      {:padding-bottom (safe-area/get-bottom)}
       :left-action          (if with-decimals? :dot :none)
       :delete-key?          true
       :on-press             (fn [c]
                               (set-input-state #(controlled-input/add-character % c)))
       :on-delete            (fn []
                               (set-input-state controlled-input/delete-last))
       :on-long-press-delete (fn []
                               (set-input-state controlled-input/delete-all))}]]))
