(ns status-im.ui.screens.wallet.manage-connections.views
  (:require
    [re-frame.core :as re-frame]
    [react-native.core :as rn]
    [status-im.ui.screens.wallet.manage-connections.styles :as styles]
    [status-im2.common.not-implemented :as not-implemented]))

(defn print-session-info
  [{:keys [peer]}]
  ^{:key peer}
  [rn/view
   [:<>
    [not-implemented/not-implemented
     [rn/view {:style styles/app-row}]]]])

(defn views
  []
  (let [sessions @(re-frame/subscribe [:wallet-connect/sessions])]
    [rn/view {:margin-top 10}
     (doall (map print-session-info sessions))]))
