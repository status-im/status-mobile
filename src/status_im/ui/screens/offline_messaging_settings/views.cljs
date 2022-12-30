(ns status-im.ui.screens.offline-messaging-settings.views
  (:require-macros [status-im.utils.views :as views])
  (:require [quo.core :as quo]
            [quo.design-system.colors :as colors]
            [re-frame.core :as re-frame]
            [i18n.i18n :as i18n]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.offline-messaging-settings.styles :as styles]
            [status-im.ui.screens.profile.components.views :as profile.components]))

(defn pinned-state
  [pinned?]
  [react/view {:style styles/automatic-selection-container}
   [react/view {:style styles/switch-container}
    [profile.components/settings-switch-item
     {:label-kw  :t/mailserver-automatic
      :value     (not pinned?)
      :action-fn #(if pinned?
                    (re-frame/dispatch [:mailserver.ui/unpin-pressed])
                    (re-frame/dispatch [:mailserver.ui/pin-pressed]))}]]
   [react/view {:style {:padding-horizontal 16}}
    [react/text {:style styles/explanation-text}
     (i18n/label :t/mailserver-automatic-switch-explanation)]]])

(defn render-row
  [{:keys [name id custom]} _ _ {:keys [current-mailserver-id preferred-mailserver-id]}]
  (let [pinned?    preferred-mailserver-id
        connected? (= id current-mailserver-id)
        visible?   (or pinned? ; show everything when auto selection is turned off
                       (and (not pinned?) ; auto selection turned on
                            (= current-mailserver-id id)))] ; show only the selected server

    (when visible?
      [react/touchable-highlight
       {:on-press            (when pinned?
                               #(if custom
                                  (re-frame/dispatch [:mailserver.ui/custom-mailserver-selected id])
                                  (re-frame/dispatch [:mailserver.ui/default-mailserver-selected id])))
        :accessibility-label :mailserver-item}
       [react/view (styles/mailserver-item)
        [react/text {:style styles/mailserver-item-name-text}
         name]

        (if pinned?
          [quo/radio {:value connected?}]
          [icons/icon :check {:color colors/blue}])]])))

(views/defview offline-messaging-settings
  []
  (views/letsubs [current-mailserver-id      [:mailserver/current-id]
                  preferred-mailserver-id    [:mailserver/preferred-id]
                  mailservers                [:mailserver/fleet-mailservers]
                  {:keys [use-mailservers?]} [:multiaccount]]
    [react/view {:style styles/wrapper}
     [topbar/topbar
      {:title             (i18n/label :t/history-nodes)
       :right-accessories
       [{:icon     :main-icons/add-circle
         :on-press #(re-frame/dispatch [:mailserver.ui/add-pressed])}]}]

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
         [pinned-state preferred-mailserver-id]

         [react/text {:style styles/history-nodes-label}
          (i18n/label :t/history-nodes)]
         [list/flat-list
          {:data               (vals mailservers)
           :default-separator? false
           :key-fn             :name
           :render-data        {:current-mailserver-id   current-mailserver-id
                                :preferred-mailserver-id preferred-mailserver-id}
           :render-fn          render-row}]])]]))
