(ns status-im.ui.screens.bootnodes-settings.edit-bootnode.views
  (:require [clojure.string :as string]
            [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.toolbar :as toolbar]
            [quo.core :as quo]
            [status-im.qr-scanner.core :as qr-scanner]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.screens.bootnodes-settings.edit-bootnode.styles
             :as
             styles])
  (:require-macros [status-im.utils.views :as views]))

(defn delete-button [id]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:bootnodes.ui/delete-pressed id])}
   [react/view styles/button-container
    [react/view {:style               styles/delete-button
                 :accessibility-label :bootnode-delete-button}
     [react/text {:style styles/button-label}
      (i18n/label :t/delete)]]]])

(views/defview edit-bootnode []
  (views/letsubs [manage-bootnode   [:get-manage-bootnode]
                  validation-errors [:manage-bootnode-validation-errors]]
    (let [url          (get-in manage-bootnode [:url :value])
          id           (get-in manage-bootnode [:id :value])
          name         (get-in manage-bootnode [:name :value])
          is-valid?    (empty? validation-errors)
          invalid-url? (contains? validation-errors :url)]
      [react/keyboard-avoiding-view {:flex 1}
       [topbar/topbar {:title (i18n/label (if id :t/bootnode-details :t/add-bootnode))}]
       [react/scroll-view {:keyboard-should-persist-taps :handled}
        [react/view styles/edit-bootnode-view
         [react/view {:padding-vertical 8}
          [quo/text-input
           {:label               (i18n/label :t/name)
            :placeholder         (i18n/label :t/specify-name)
            :accessibility-label :bootnode-name
            :default-value       name
            :on-change-text      #(re-frame/dispatch [:bootnodes.ui/input-changed :name %])
            :auto-focus          true}]]
         [react/view
          {:flex             1
           :padding-vertical 8}
          [quo/text-input
           (merge
            {:label               (i18n/label :t/bootnode-address)
             :placeholder         (i18n/label :t/bootnode-format)
             :accessibility-label :bootnode-address
             :default-value       url
             :show-cancel         false
             :on-change-text      #(re-frame/dispatch [:bootnodes.ui/input-changed :url %])
             :error               (when (and (not (string/blank? url)) invalid-url?)
                                    (i18n/label :t/invalid-format
                                                {:format (i18n/label :t/bootnode-format)}))
             :bottom-value        0
             :after               {:icon     :main-icons/qr
                                   :on-press #(re-frame/dispatch [::qr-scanner/scan-code
                                                                  {:title   (i18n/label :t/add-bootnode)
                                                                   :handler :bootnodes.callback/qr-code-scanned}])}})]]
         (when id
           [delete-button id])]]
       [toolbar/toolbar
        {:right
         [quo/button
          {:type      :secondary
           :after     :main-icon/next
           :disabled  (not is-valid?)
           :on-press  #(re-frame/dispatch [:bootnodes.ui/save-pressed])}
          (i18n/label :t/save)]}]])))
