(ns status-im.ui.screens.offline-messaging-settings.edit-mailserver.views
  (:require-macros [status-im.utils.views :as views])
  (:require
   [re-frame.core :as re-frame]
   [status-im.ui.components.react :as react]
   [status-im.i18n :as i18n]
   [status-im.ui.components.colors :as colors]
   [status-im.ui.components.icons.vector-icons :as vector-icons]
   [status-im.ui.components.styles :as components.styles]
   [status-im.ui.components.common.common :as components.common]
   [status-im.ui.screens.offline-messaging-settings.edit-mailserver.styles :as styles]
   [clojure.string :as string]
   [quo.core :as quo]
   [status-im.ui.components.topbar :as topbar]))

(defn connect-button [id]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:mailserver.ui/connect-pressed id])}
   [react/view styles/button-container
    [react/view {:style               styles/connect-button
                 :accessibility-label :mailserver-connect-button}
     [react/text {:style styles/button-label}
      (i18n/label :t/connect)]]]])

(defn delete-button [id]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:mailserver.ui/delete-pressed id])}
   [react/view styles/button-container
    [react/view {:style               styles/delete-button
                 :accessibility-label :mailserver-delete-button}
     [react/text {:style styles/button-label}
      (i18n/label :t/delete)]]]])

(def qr-code
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:qr-scanner.ui/scan-qr-code-pressed
                                                             {:title (i18n/label :t/add-mailserver)
                                                              :handler :mailserver.callback/qr-code-scanned}])
                              :style    styles/qr-code}
   [react/view
    [vector-icons/icon :main-icons/qr {:color colors/blue}]]])

(views/defview edit-mailserver []
  (views/letsubs [mailserver [:mailserver.edit/mailserver]
                  connected? [:mailserver.edit/connected?]
                  validation-errors [:mailserver.edit/validation-errors]]
    (let [url          (get-in mailserver [:url :value])
          id           (get-in mailserver [:id :value])
          name         (get-in mailserver [:name :value])
          is-valid?    (and (not (string/blank? url))
                            (not (string/blank? name))
                            (empty? validation-errors))
          invalid-url? (contains? validation-errors :url)]
      [react/view components.styles/flex
       [react/keyboard-avoiding-view components.styles/flex
        [topbar/topbar {:title (if id :t/mailserver-details :t/add-mailserver)}]
        [react/scroll-view {:keyboard-should-persist-taps :handled}
         [react/view styles/edit-mailserver-view
          [react/view {:padding-vertical 8}
           [quo/text-input
            {:label          (i18n/label :t/name)
             :placeholder    (i18n/label :t/specify-name)
             :default-value  name
             :on-change-text #(re-frame/dispatch [:mailserver.ui/input-changed :name %])
             :auto-focus     true}]]
          [react/view {:flex             1
                       :padding-vertical 8}
           [quo/text-input
            {:label          (i18n/label :t/mailserver-address)
             :placeholder    (i18n/label :t/mailserver-format)
             :content        qr-code
             :default-value  url
             :on-change-text #(re-frame/dispatch [:mailserver.ui/input-changed :url %])
             :bottom-value   0
             :error          (when (and (not (string/blank? url))
                                        invalid-url?)
                               (i18n/label :t/invalid-format
                                           {:format (i18n/label :t/mailserver-format)}))}]]
          (when (and id
                     (not connected?))
            [react/view
             [connect-button id]
             [delete-button id]])]]
        [react/view styles/bottom-container
         [react/view components.styles/flex]
         [components.common/bottom-button
          {:forward?  true
           :label     (i18n/label :t/save)
           :disabled? (not is-valid?)
           :on-press  #(re-frame/dispatch [:mailserver.ui/save-pressed])}]]]])))
