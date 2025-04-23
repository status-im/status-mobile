(ns status-im.contexts.wallet.wallet-connect.modals.change-network.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.wallet-connect.modals.common.header.components :as header]
    [status-im.contexts.wallet.wallet-connect.modals.session-proposal.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.string]))

(defn- on-dismiss
  []
  (rf/dispatch [:hide-bottom-sheet]))

(defn- make-list-item
  [customization-color set-deactivate-chain-id deactivate-chain-id network]
  (let [{:keys [full-name source chain-id]} network
        on-select                           #(set-deactivate-chain-id chain-id)
        selected?                           (= deactivate-chain-id chain-id)]
    {:blur?        true
     :action       :selector
     :action-props {:type                :radio
                    :blur?               false
                    :customization-color customization-color
                    :checked?            selected?
                    :on-change           on-select}
     :title        full-name
     :image        :icon-avatar
     :image-props  {:icon source
                    :size :size-20}}))

(defn view
  [{:keys [on-success]}]
  (let [max-active-networks?                          (rf/sub [:wallet/max-active-networks-reached?])
        dapp                                          (rf/sub [:wallet-connect/current-request-dapp])
        dapp-icon                                     (rf/sub [:wallet-connect/dapp-icon])
        activate-network                              (rf/sub [:wallet-connect/current-request-network])
        deactivatable-networks                        (rf/sub [:wallet/deactivatable-networks])
        {:keys [customization-color]}                 (rf/sub
                                                       [:wallet-connect/current-request-account-details])
        default-deactivate-chain-id                   (-> deactivatable-networks first :chain-id)
        [deactivate-chain-id set-deactivate-chain-id] (rn/use-state (when max-active-networks?
                                                                      default-deactivate-chain-id))
        success-pressed?                              (rn/use-ref-atom false)
        on-press-success                              (fn []
                                                        (reset! success-pressed? true)
                                                        (rf/dispatch
                                                         [:wallet-connect/activate-request-network
                                                          {:deactivate-chain-id deactivate-chain-id}])
                                                        (rf/dispatch [:hide-bottom-sheet])
                                                        ;; FIXME: not the most reliable way to make
                                                        ;; sure the balance was updated on the
                                                        ;; status-go side
                                                        (when on-success
                                                          (js/setTimeout on-success 1000)))]
    (rn/use-unmount (fn []
                      (when-not @success-pressed?
                        (rf/dispatch [:wallet-connect/on-request-modal-dismissed]))))
    [rn/view {:style {:padding-horizontal 20}}
     [rn/view {:style {:margin-bottom 20}}
      [header/title-container
       [header/title-text {:text (i18n/label :t/activate)}]
       [header/title-summary
        {:type         :network
         :label        (:full-name activate-network)
         :image-source (:source activate-network)}]
       [header/title-text {:text (i18n/label :t/enable-network-to-sign-requests)}]
       [header/title-summary
        {:type         :dapp
         :label        (:name dapp)
         :image-source dapp-icon}]]
      [quo/text
       {:weight :medium
        :size   :paragraph-2}
       (if max-active-networks?
         (i18n/label :t/dapp-requests-network-access-with-limit)
         (i18n/label :t/dapp-requests-network-access))]]
     (when max-active-networks?
       [quo/category
        {:list-type       :settings
         :blur?           true
         :data            (mapv (partial make-list-item
                                         customization-color
                                         set-deactivate-chain-id
                                         deactivate-chain-id)
                                deactivatable-networks)
         :container-style {:padding-horizontal 0}}])
     [quo/bottom-actions
      {:actions                 :two-actions
       :buttons-container-style style/footer-buttons-container
       :button-two-label        (i18n/label :t/decline)
       :button-two-props        {:type     :grey
                                 :on-press on-dismiss}
       :button-one-label        (i18n/label :t/confirm)
       :button-one-props        {:customization-color customization-color
                                 :type                :primary
                                 :on-press            on-press-success}}]]))
