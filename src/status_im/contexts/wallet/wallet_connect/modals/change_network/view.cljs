(ns status-im.contexts.wallet.wallet-connect.modals.change-network.view
  (:require
    [clojure.string :as string]
    [quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.wallet-connect.modals.common.header.components :as header]
    [status-im.contexts.wallet.wallet-connect.modals.common.list-info-box.view :as list-info-box]
    [status-im.contexts.wallet.wallet-connect.modals.session-proposal.style :as style]
    [status-im.contexts.wallet.wallet-connect.utils.data-store :as data-store]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]
    [utils.string]))

(defn- on-dismiss
  []
  (rf/dispatch [:hide-bottom-sheet]))

(defn- on-press-activate
  [props]
  (rf/dispatch [:wallet-connect/adapt-network props])
  (rf/dispatch [:hide-bottom-sheet]))

(defn- dapp-request-description
  [{:keys [dapp]}]
  []
  [rn/view {:style {:flex-direction :row :flex-wrap :wrap}}
   [quo/context-tag
    {:size      24
     :type      :dapp
     :dapp-logo (data-store/compute-dapp-icon-path (:iconUrl dapp) (:url dapp))
     :dapp-name (:name dapp)}]
   [:<>
    (->> (string/split (i18n/label :t/dapp-requests-network-access) #" ")
         (map
          (fn [word]
            ^{:key word}
            [rn/view
             {:style {:height          21.75
                      :justify-content :center}}
             [quo/text
              {:weight :medium
               :size   :paragraph-2}
              (str " " word)]]))
         (doall))]])

(defn view
  [{:keys [activate-chain-id deactivate-chain-id on-success] :as props}]
  (let [dapp               (rf/sub [:wallet-connect/current-request-dapp])
        activate-network   (rf/sub [:wallet/network-by-id activate-chain-id])
        deactivate-network (when deactivate-chain-id
                             (rf/sub [:wallet/network-by-id deactivate-chain-id]))
        account            (rf/sub [:wallet-connect/current-request-account-details])
        success-pressed?   (rn/use-ref-atom false)
        on-press-success   (fn []
                             (reset! success-pressed? true)
                             (on-press-activate props)
                             ;; NOTE: make sure the balance was updated on status-go side
                             (js/setTimeout on-success 2000))]
    (rn/use-unmount (fn []
                      (when-not @success-pressed?
                        (rf/dispatch [:wallet-connect/on-request-modal-dismissed]))))
    [rn/view
     [quo/page-nav
      {:icon-name  :i/close
       :background :blur
       :on-press   on-dismiss}]
     [rn/view {:style {:padding-horizontal 20}}
      [rn/view {:style {:margin-bottom 20}}
       [header/title-container
        [header/title-text {:text (i18n/label :t/enable)}]
        [header/title-summary
         {:type         :network
          :label        (:full-name activate-network)
          :image-source (:source activate-network)}]
        [header/title-text {:text (i18n/label :t/enable-network-to-sign-requests)}]]
       [dapp-request-description {:dapp dapp}]
       (when deactivate-network
         [quo/information-box
          {:type  :default
           :icon  :i/info
           :style {:margin-top 16}}
          (i18n/label :t/dapp-network-limit-infobox {:network (:full-name deactivate-network)})])
       [list-info-box/view
        {:dapp-name       (:name dapp)
         :container-style {:margin-top 20}}]]
      [quo/bottom-actions
       {:actions                 :two-actions
        :buttons-container-style style/footer-buttons-container
        :button-two-label        (i18n/label :t/decline)
        :button-two-props        {:type                :grey
                                  :accessibility-label :wc-deny-connection
                                  :on-press            on-dismiss}
        :button-one-label        (i18n/label :t/enable)
        :button-one-props        {:customization-color (:customization-color account)
                                  :type                :primary
                                  :accessibility-label :wc-connect
                                  :on-press            on-press-success}}]]]))
