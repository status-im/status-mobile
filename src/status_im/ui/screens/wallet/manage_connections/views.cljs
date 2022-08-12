(ns status-im.ui.screens.wallet.manage-connections.views
  (:require [re-frame.core :as re-frame]
            [quo.react-native :as rn]
            [status-im.i18n.i18n :as i18n]
            [quo.core :as quo]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.wallet.manage-connections.styles :as styles]
            [quo.design-system.colors :as colors]
            [status-im.ui.components.icons.icons :as icons]
            [taoensso.timbre :as log]
            [status-im.utils.utils :as utils]))

(defn print-session-info [session]
  (let [params (get session :params)
        peerMeta (get (first params) :peerMeta)
        name (get peerMeta :name)
        url (get peerMeta :url)
        description (get peerMeta :description)
        icons (get peerMeta :icons)
        filtered-icons (status-im.utils.utils/exclude-svg-resources icons)
        icon-uri (when (and filtered-icons (pos? (count filtered-icons))) (first filtered-icons))
        visible-accounts @(re-frame/subscribe [:visible-accounts-without-watch-only])
        dapps-account @(re-frame/subscribe [:dapps-account])
        account-name (get dapps-account :name)
        account-background-color (get dapps-account :color)]
    [rn/view
     [:<>
      [rn/view {:style styles/app-row}
       [react/image {:style styles/daap-icon :source {:uri icon-uri}}]
       [rn/view {:style styles/app-column}
        [rn/text {:style styles/daap-name} name]
        [rn/text {:style styles/daap-url} url]]

       [rn/touchable-opacity {:style styles/delete-icon-container
                              :on-press #(re-frame/dispatch [:wallet-connect-legacy/disconnect session])}

        [icons/icon :icons/delete {:width 20
                                   :height 20
                                   :container-style {:elevation 3}
                                   :color colors/red}]]

       [rn/touchable-opacity {:style (styles/selected-account-container account-background-color)}
        [rn/text {:style styles/selected-account} account-name]
        [icons/icon :icons/dropdown  {:width 20
                                      :height 20
                                      :container-style {:elevation 3}
                                      :color colors/white}]]]]]))

(defn views []
  (let [legacy-sessions @(re-frame/subscribe [:wallet-connect-legacy/sessions])
        how-many-sessions (count legacy-sessions)]
    [rn/view {:margin-top 10}
     (map print-session-info legacy-sessions)
     [rn/text {:style {:font-size 12 :margin 10}} (str "Total wallets connected : " how-many-sessions)]]))

