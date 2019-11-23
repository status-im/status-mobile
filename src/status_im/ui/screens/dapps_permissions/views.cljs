(ns status-im.ui.screens.dapps-permissions.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.dapps-permissions.styles :as styles]
            [status-im.constants :as constants]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.icons.vector-icons :as icons]))

(defn d-icon []
  [react/view styles/icon-container
   [icons/icon :main-icons/dapp {:color colors/gray}]])

(defn prepare-items [{:keys [dapp permissions]}]
  {:title       dapp
   :accessories [:chevron]
   :on-press    #(re-frame/dispatch [:navigate-to :manage-dapps-permissions {:dapp dapp :permissions permissions}])
   :icon        d-icon})

(defn prepare-items-manage [name]
  (fn [permission]
    {:title       (case permission
                    constants/dapp-permission-web3         name
                    constants/dapp-permission-contact-code :t/contact-code)
     :type        :small
     :accessories [:main-icons/check]}))

(views/defview dapps-permissions []
  (views/letsubs [permissions [:dapps/permissions]]
    [react/view {:flex 1 :background-color colors/white}
     [toolbar/simple-toolbar
      (i18n/label :t/dapps-permissions)]
     [list/flat-list
      {:data      (vec (map prepare-items (vals permissions)))
       :key-fn    (fn [_ i] (str i))
       :render-fn list/flat-list-generic-render-fn}]]))

(views/defview manage []
  (views/letsubs [{:keys [dapp permissions]} [:get-screen-params]
                  {:keys [name]} [:dapps-account]]
    [react/view {:flex 1 :background-color colors/white}
     [toolbar/simple-toolbar dapp]
     [list/flat-list
      {:data      (vec (map (prepare-items-manage name) permissions))
       :key-fn    (fn [_ i] (str i))
       :render-fn list/flat-list-generic-render-fn}]
     [react/view {:padding-vertical 16}
      [components.common/red-button {:label    (i18n/label :t/revoke-access)
                                     :on-press #(re-frame/dispatch [:dapps/revoke-access dapp])}]]]))
