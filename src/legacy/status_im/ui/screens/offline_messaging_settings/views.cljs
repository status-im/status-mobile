(ns legacy.status-im.ui.screens.offline-messaging-settings.views
  (:require-macros [legacy.status-im.utils.views :as views])
  (:require
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.list.views :as list]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.topbar :as topbar]
    [legacy.status-im.ui.screens.offline-messaging-settings.styles :as styles]
    [legacy.status-im.ui.screens.profile.components.views :as profile.components]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]))

(defn render-row
  [{:keys [id name]} _ _ {:keys [current-mailserver-id]}]
  (let [visible? (= current-mailserver-id id)]
    (when visible?
      [react/touchable-highlight {:accessibility-label :mailserver-item}
       [react/view (styles/mailserver-item)
        [react/text {:style styles/mailserver-item-name-text}
         name]
        [icons/icon :check {:color colors/blue}]]])))

(views/defview offline-messaging-settings
  []
  (views/letsubs [current-mailserver-id      [:mailserver/current-id]
                  mailservers                [:mailserver/fleet-mailservers]
                  {:keys [use-mailservers?]} [:profile/profile]]
    [react/view {:style styles/wrapper}
     [topbar/topbar {:title (i18n/label :t/history-nodes)}]

     [react/scroll-view
      [react/view {:style styles/switch-container}
       [profile.components/settings-switch-item
        {:label-kw  :t/offline-messaging-use-history-nodes
         :value     use-mailservers?
         :action-fn #(re-frame/dispatch [:mailserver.ui/use-history-switch-pressed
                                         (not use-mailservers?)])}]]
      [react/view {:style styles/use-history-explanation-text-container}
       [react/text {:style styles/explanation-text}
        (i18n/label :t/offline-messaging-use-history-explanation)]]

      (when use-mailservers?
        [:<>
         [react/text {:style styles/history-nodes-label}
          (i18n/label :t/history-nodes)]
         [list/flat-list
          {:data               (vals mailservers)
           :default-separator? false
           :key-fn             :name
           :render-data        {:current-mailserver-id current-mailserver-id}
           :render-fn          render-row}]])]]))
