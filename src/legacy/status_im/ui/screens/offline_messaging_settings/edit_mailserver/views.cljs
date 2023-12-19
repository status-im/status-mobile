(ns legacy.status-im.ui.screens.offline-messaging-settings.edit-mailserver.views
  (:require-macros [legacy.status-im.utils.views :as views])
  (:require
    [clojure.string :as string]
    [legacy.status-im.qr-scanner.core :as qr-scanner]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.toolbar :as toolbar]
    [legacy.status-im.ui.components.topbar :as topbar]
    [legacy.status-im.ui.screens.offline-messaging-settings.edit-mailserver.styles :as styles]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n]))

(defn connect-button
  [id]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:mailserver.ui/connect-pressed id])}
   [react/view styles/button-container
    [react/view
     {:style               styles/connect-button
      :accessibility-label :mailserver-connect-button}
     [react/text {:style styles/button-label}
      (i18n/label :t/connect)]]]])

(defn delete-button
  [id]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:mailserver.ui/delete-pressed id])}
   [react/view styles/button-container
    [react/view
     {:style               styles/delete-button
      :accessibility-label :mailserver-delete-button}
     [react/text {:style styles/button-label}
      (i18n/label :t/delete)]]]])

(views/defview edit-mailserver
  []
  (views/letsubs [mailserver        [:mailserver.edit/mailserver]
                  connected?        [:mailserver.edit/connected?]
                  validation-errors [:mailserver.edit/validation-errors]]
    (let [url          (get-in mailserver [:url :value])
          id           (get-in mailserver [:id :value])
          name         (get-in mailserver [:name :value])
          is-valid?    (and (not (string/blank? url))
                            (not (string/blank? name))
                            (empty? validation-errors))
          invalid-url? (contains? validation-errors :url)]
      [react/keyboard-avoiding-view
       {:style         {:flex 1}
        :ignore-offset true}
       [topbar/topbar {:title (i18n/label (if id :t/mailserver-details :t/add-mailserver))}]
       [react/scroll-view {:keyboard-should-persist-taps :handled}
        [react/view styles/edit-mailserver-view
         [react/view {:padding-vertical 8}
          [quo/text-input
           {:label          (i18n/label :t/name)
            :placeholder    (i18n/label :t/specify-name)
            :default-value  name
            :on-change-text #(re-frame/dispatch [:mailserver.ui/input-changed :name %])
            :auto-focus     true}]]
         [react/view
          {:flex             1
           :padding-vertical 8}
          [quo/text-input
           {:label          (i18n/label :t/mailserver-address)
            :placeholder    (i18n/label :t/mailserver-format)
            :default-value  url
            :show-cancel    false
            :on-change-text #(re-frame/dispatch [:mailserver.ui/input-changed :url %])
            :bottom-value   0
            :error          (when (and (not (string/blank? url))
                                       invalid-url?)
                              (i18n/label :t/invalid-format
                                          {:format (i18n/label :t/mailserver-format)}))
            :after          {:icon     :main-icons/qr
                             :on-press #(re-frame/dispatch
                                         [::qr-scanner/scan-code
                                          {:title   (i18n/label :t/add-mailserver)
                                           :handler :mailserver.callback/qr-code-scanned}])}}]]
         (when (and id
                    (not connected?))
           [react/view
            [connect-button id]
            [delete-button id]])]]
       [toolbar/toolbar
        {:right
         [quo/button
          {:type     :secondary
           :after    :main-icon/next
           :disabled (not is-valid?)
           :on-press #(re-frame/dispatch [:mailserver.ui/save-pressed])}
          (i18n/label :t/save)]}]])))
