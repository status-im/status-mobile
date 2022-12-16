(ns status-im.ui.screens.wallet.manage-connections.views
  (:require [re-frame.core :as re-frame]
            [quo.react-native :as rn]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.manage-connections.styles :as styles]
            [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.utils.utils :as utils]))

(defn print-session-info [session]
  (let [peer-meta (get-in session [:params 0 :peerMeta])
        peer-id (get-in session [:params 0 :peerId])
        name (get peer-meta :name)
        url (get peer-meta :url)
        account (get-in session [:params 0  :accounts 0])
        icons (get peer-meta :icons)
        icon-uri (first (status-im.utils.utils/exclude-svg-resources icons))
        visible-accounts @(re-frame/subscribe [:visible-accounts-without-watch-only])
        selected-account (first (filter
                                 #(= account
                                     (:address %))
                                 visible-accounts))]
    ^{:key peer-id}
    [rn/view
     [:<>
      [rn/view {:style styles/app-row}
       [react/image {:style styles/dapp-icon :source {:uri icon-uri}}]
       [rn/view {:style styles/app-column}
        [quo/text {:style styles/dapp-name} name]
        [quo/text {:style styles/dapp-url} url]]

       [rn/view {:flex-direction :row
                 :position :absolute
                 :right 10
                 :align-items :center}
        [rn/touchable-opacity {:style styles/delete-icon-container
                               :on-press #(re-frame/dispatch [:wallet-connect-legacy/disconnect session])}

         [icons/icon :icons/delete {:width 20
                                    :height 20
                                    :container-style {:elevation 3}
                                    :color colors/red}]]

        (when selected-account ;; The account might not be available in theory, if deleted
          [rn/view {:style (styles/selected-account-container (:color selected-account))}
           [rn/text {:style styles/selected-account} (:name selected-account)]])]]]]))

(defn views []
  (let [legacy-sessions @(re-frame/subscribe [:wallet-connect-legacy/sessions])]
    [rn/view {:margin-top 10}
     (doall (map print-session-info legacy-sessions))]))
