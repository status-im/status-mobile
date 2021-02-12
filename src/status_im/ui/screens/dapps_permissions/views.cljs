(ns status-im.ui.screens.dapps-permissions.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.constants :as constants]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.dapps-permissions.styles :as styles]
            [quo.core :as quo]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.topbar :as topbar]))

(defn d-icon []
  [react/view styles/icon-container
   [icons/icon :main-icons/dapp {:color colors/gray}]])

(defn prepare-items [{:keys [dapp permissions]}]
  {:title    dapp
   :chevron  true
   :on-press #(re-frame/dispatch [:navigate-to :manage-dapps-permissions {:dapp dapp :permissions permissions}])
   :icon     [d-icon]})

(defn prepare-items-manage [name]
  (fn [permission]
    {:title     (cond
                  (= permission constants/dapp-permission-web3)
                  name
                  (= permission constants/dapp-permission-contact-code)
                  (i18n/label :t/contact-code))
     :size      :small
     :accessory [icons/icon :main-icons/check {}]}))

(views/defview dapps-permissions []
  (views/letsubs [permissions [:dapps/permissions]]
    [react/view {:flex 1 :background-color colors/white}
     [topbar/topbar {:title (i18n/label :t/dapps-permissions)}]
     [list/flat-list
      {:data      (vec (map prepare-items (vals permissions)))
       :key-fn    (fn [_ i] (str i))
       :render-fn quo/list-item}]]))

(views/defview manage []
  (views/letsubs [{:keys [dapp permissions]} [:get-screen-params]
                  {:keys [name]} [:dapps-account]]
    [react/view {:flex 1 :background-color colors/white}
     [topbar/topbar {:title dapp}]
     [list/flat-list
      {:data      (vec (map (prepare-items-manage name) permissions))
       :key-fn    (fn [_ i] (str i))
       :render-fn quo/list-item}]
     [react/view {:padding-vertical   16
                  :padding-horizontal 16}
      [quo/button {:theme    :negative
                   :on-press #(re-frame/dispatch [:dapps/revoke-access dapp])}
       (i18n/label :t/revoke-access)]]]))
