(ns status-im.ui.screens.offline-messaging-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.utils.config :as config]
            [status-im.ui.components.checkbox.view :as checkbox.views]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.utils.platform :as platform]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.screens.offline-messaging-settings.styles :as styles]))

(defn- mailserver-icon [connected?]
  [react/view (if platform/desktop?
                {:style (styles/mailserver-icon-container connected?)}
                (styles/mailserver-icon-container connected?))
   [vector-icons/icon :main-icons/mailserver
    (if platform/desktop? {:style (styles/mailserver-icon connected?)}
        (styles/mailserver-icon connected?))]])

(defn pinned-state [pinned?]
  [react/touchable-highlight {:on-press (if pinned?
                                          #(re-frame/dispatch [:mailserver.ui/unpin-pressed])
                                          #(re-frame/dispatch [:mailserver.ui/pin-pressed]))}
   [react/view {:style styles/mailserver-pinned}
    [checkbox.views/checkbox
     {:checked? (not pinned?)
      :on-value-change (if pinned?
                         #(re-frame/dispatch [:mailserver.ui/unpin-pressed])
                         #(re-frame/dispatch [:mailserver.ui/pin-pressed]))}]
    [react/view  {:style styles/mailserver-pinned-text-container}
     [react/text {:style styles/mailserver-pinned-text}
      (i18n/label :t/mailserver-automatic)]]]])

(defn render-row [current-mailserver-id pinned?]
  (fn [{:keys [name id user-defined]}]
    (let [connected? (= id current-mailserver-id)]
      [react/touchable-highlight
       {:on-press            (when pinned? #(if user-defined
                                              (re-frame/dispatch [:mailserver.ui/user-defined-mailserver-selected id])
                                              (re-frame/dispatch [:mailserver.ui/default-mailserver-selected id])))
        :accessibility-label :mailserver-item}
       [react/view (styles/mailserver-item pinned?)
        [mailserver-icon connected?]
        [react/view styles/mailserver-item-inner
         [react/text {:style styles/mailserver-item-name-text}
          name]]]])))

(views/defview offline-messaging-settings []
  (views/letsubs [current-mailserver-id   [:mailserver/current-id]
                  preferred-mailserver-id [:mailserver/preferred-id]
                  mailservers             [:mailserver/fleet-mailservers]]
    [react/view {:flex 1}
     [status-bar/status-bar]
     [toolbar/toolbar {}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/offline-messaging-settings)]
      [toolbar/actions
       [(toolbar.actions/add false #(re-frame/dispatch [:mailserver.ui/add-pressed]))]]]
     [react/view styles/wrapper
      [pinned-state preferred-mailserver-id]
      [list/flat-list {:data               (vals mailservers)
                       :default-separator? false
                       :key-fn             :name
                       :render-fn          (render-row current-mailserver-id
                                                       preferred-mailserver-id)}]]]))
