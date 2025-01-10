(ns status-im.contexts.wallet.send.transaction-settings.view
  (:require
    [quo.core :as quo]
    [react-native.core :as rn]
    [react-native.platform :as platform]
    [react-native.safe-area :as safe-area]
    [status-im.common.controlled-input.utils :as controlled-input]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))


(defn custom-settings-sheet
  [_]
  (let [max-base-fee   (:current (rf/sub [:wallet/tx-settings-max-base-fee]))
        priority-fee   (:current (rf/sub [:wallet/tx-settings-priority-fee]))
        max-gas-amount (:current (rf/sub [:wallet/tx-settings-max-gas-amount]))
        nonce          (:current (rf/sub [:wallet/tx-settings-nonce]))]
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
                                                       :screen/wallet.transaction-confirmation]])
                    :label             :text
                    :preview-size      :size-32}
                   {:title             (i18n/label :t/priority-fee)
                    :description-props {:text (str priority-fee " GWEI")}
                    :image             :none
                    :description       :text
                    :action            :arrow
                    :on-press          #(rf/dispatch [:navigate-to-within-stack
                                                      [:screen/wallet.tx-settings-priority-fee
                                                       :screen/wallet.transaction-confirmation]])
                    :label             :text
                    :preview-size      :size-32}
                   {:title             (i18n/label :t/max-gas-amount)
                    :description-props {:text (str max-gas-amount " UNITS")}
                    :image             :none
                    :description       :text
                    :action            :arrow
                    :on-press          #(rf/dispatch [:navigate-to-within-stack
                                                      [:screen/wallet.tx-settings-gas-amount
                                                       :screen/wallet.transaction-confirmation]])
                    :label             :text
                    :preview-size      :size-32}
                   {:title             (i18n/label :t/nonce)
                    :description-props {:text nonce}
                    :image             :none
                    :description       :text
                    :action            :arrow
                    :on-press          #(rf/dispatch [:navigate-to-within-stack
                                                      [:screen/wallet.tx-settings-nonce
                                                       :screen/wallet.transaction-confirmation]])
                    :label             :text
                    :preview-size      :size-32}]}]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-props {:on-press #(rf/dispatch [:hide-bottom-sheet])}
       :button-one-label (i18n/label :t/confirm)}]]))

(defn settings-sheet
  [_]
  (let [[selected-id set-selected-id] (rn/use-state :normal)]
    [rn/view
     [quo/drawer-top
      {:title (i18n/label :t/transaction-settings)}]
     [quo/category
      {:list-type :settings
       :data      [{:title             (str (i18n/label :t/normal) "~60s")
                    :image-props       "🍿"
                    :description-props {:text "€1.45"}
                    :image             :emoji
                    :description       :text
                    :action            :selector
                    :action-props      {:type     :radio
                                        :checked? (= :normal selected-id)}
                    :on-press          #(set-selected-id :normal)
                    :label             :text
                    :preview-size      :size-32}
                   {:title             (str (i18n/label :t/fast) "~40s")
                    :image-props       "🚗"
                    :description-props {:text "€1.65"}
                    :image             :emoji
                    :description       :text
                    :action            :selector
                    :action-props      {:type     :radio
                                        :checked? (= :fast selected-id)}
                    :on-press          #(set-selected-id :fast)
                    :label             :text
                    :preview-size      :size-32}
                   {:title             (str (i18n/label :t/urgent) "~15s")
                    :image-props       "🚀"
                    :description-props {:text "€1.85"}
                    :image             :emoji
                    :description       :text
                    :action            :selector
                    :action-props      {:type     :radio
                                        :checked? (= :urgent selected-id)}
                    :on-press          #(set-selected-id :urgent)
                    :label             :text
                    :preview-size      :size-32}
                   {:title             (i18n/label :t/custom)
                    :image-props       :i/edit
                    :description-props {:text "Set your own fees and nonce"}
                    :image             :icon
                    :description       :text
                    :action            :arrow
                    :on-press          #(rf/dispatch
                                         [:show-bottom-sheet
                                          {:content custom-settings-sheet}])
                    :label             :text
                    :preview-size      :size-32}]}]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-props {:on-press #(rf/dispatch [:hide-bottom-sheet])}
       :button-one-label (i18n/label :t/confirm)}]]))

(defn- hint
  [{:keys [error? text]}]
  [quo/network-tags
   {:title  text
    :status (when error? :error)}])

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
      :on-press        #()}
     (i18n/label :t/read-more)]]])

(defn custom-setting-screen
  [{:keys [screen-title token-symbol hint-text-fn suggested-values info-title info-content on-save
           with-decimals?]
    :or   {with-decimals? true}}]
  (let [[input-state set-input-state] (rn/use-state (controlled-input/set-value-numeric
                                                     controlled-input/init-state
                                                     (:current suggested-values)))
        input-value                   (controlled-input/input-value input-state)
        out-of-limits?                (controlled-input/input-error input-state)
        valid-input?                  (not (or (controlled-input/empty-value? input-state)
                                               out-of-limits?))]
    (rn/use-mount
     (fn []

       (set-input-state (fn [state]
                          (-> state
                              (controlled-input/set-upper-limit (:high suggested-values))
                              (controlled-input/set-lower-limit (:low suggested-values)))))))
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
       :error?           out-of-limits?
       :currency-symbol  token-symbol
       :hint-component   [hint
                          {:error? out-of-limits?
                           :text   (hint-text-fn
                                    (controlled-input/lower-limit-exceeded? input-state)
                                    (controlled-input/upper-limit-exceeded? input-state))}]}]
     [rn/view {:style {:flex 1}}]
     [quo/bottom-actions
      {:actions          :one-action
       :button-one-label (i18n/label :t/save-changes)
       :button-one-props {:disabled? (not valid-input?)
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
