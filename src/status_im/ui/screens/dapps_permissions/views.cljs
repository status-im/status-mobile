(ns status-im.ui.screens.dapps-permissions.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.list-item.views :as list-item]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.screens.dapps-permissions.styles :as styles]
            [status-im.constants :as constants]
            [status-im.ui.components.common.common :as components.common]
            [status-im.ui.components.icons.vector-icons :as icons]))

(defn d-icon []
  [react/view styles/icon-container
   [icons/icon :main-icons/dapp {:color colors/gray}]])

(defn prepare-items [dapps]
  (fn [{:keys [dapp permissions]}]
    (merge
     {:title       dapp
      :accessories [:chevron]
      :on-press    #(re-frame/dispatch [:navigate-to :manage-dapps-permissions {:dapp dapp :permissions permissions}])}
     (let [path (get-in dapps [dapp :photo-path])]
       (if path
         {:image-path path}
         {:image d-icon})))))

(defn prepare-items-manage [permission]
  {:title       (case permission
                  constants/dapp-permission-web3 (i18n/label :t/wallet)
                  constants/dapp-permission-contact-code (i18n/label :t/contact-code))
   :type        :small
   :accessories [:check]})

(views/defview dapps-permissions []
  (views/letsubs [permissions [:get :dapps/permissions]
                  dapps       [:contacts/dapps-by-name]]
    [react/view {:flex 1 :background-color colors/white}
     [status-bar/status-bar]
     [toolbar/simple-toolbar
      (i18n/label :t/dapps-permissions)]
     [list/flat-list
      {:data      (map (prepare-items dapps) (vals permissions))
       :key-fn    (fn [_ i] (str i))
       :render-fn list-item/list-item}]]))

(views/defview manage []
  (views/letsubs [{:keys [dapp permissions]} [:get-screen-params]]
    [react/view {:flex 1 :background-color colors/white}
     [status-bar/status-bar]
     [toolbar/simple-toolbar dapp]
     [list/flat-list
      {:data      (map prepare-items-manage permissions)
       :key-fn    (fn [_ i] (str i))
       :render-fn list-item/list-item}]
     [react/view {:padding-vertical 16}
      [components.common/red-button {:label (i18n/label :t/revoke-access)
                                     :on-press #(re-frame/dispatch [:dapps/revoke-access dapp])}]]]))
