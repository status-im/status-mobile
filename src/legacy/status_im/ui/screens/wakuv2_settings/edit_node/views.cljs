(ns legacy.status-im.ui.screens.wakuv2-settings.edit-node.views
  (:require
    [clojure.string :as string]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.toolbar :as toolbar]
    [legacy.status-im.ui.components.topbar :as topbar]
    [legacy.status-im.ui.screens.wakuv2-settings.edit-node.styles :as styles]
    [re-frame.core :as re-frame]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :as views]))

(defn delete-button
  [id]
  [react/touchable-highlight {:on-press #(re-frame/dispatch [:wakuv2.ui/delete-pressed id])}
   [react/view styles/button-container
    [react/view
     {:style               styles/delete-button
      :accessibility-label :wakuv2-delete-button}
     [react/text {:style styles/button-label}
      (i18n/label :t/delete)]]]])

(views/defview edit-node
  []
  (views/letsubs [manage-node       [:wakuv2-nodes/manage]
                  validation-errors [:wakuv2-nodes/validation-errors]]
    (let [address          (get-in manage-node [:address :value])
          id               (:id manage-node)
          name             (get-in manage-node [:name :value])
          new-node?        (:new? manage-node)
          is-valid?        (empty? validation-errors)
          invalid-address? (contains? validation-errors :address)]
      [react/keyboard-avoiding-view
       {:style         {:flex 1}
        :ignore-offset true}
       [topbar/topbar {:title (i18n/label (if name :t/node-details :t/add-node))}]
       [react/scroll-view {:keyboard-should-persist-taps :handled}
        [react/view styles/edit-node-view
         [react/view {:padding-vertical 8}
          [quo/text-input
           {:label               (i18n/label :t/name)
            :placeholder         (i18n/label :t/specify-name)
            :accessibility-label :node-name
            :default-value       name
            :on-change-text      #(re-frame/dispatch [:wakuv2.ui/input-changed :name %])
            :auto-focus          true}]]
         [react/view
          {:flex             1
           :padding-vertical 8}
          [quo/text-input
           (merge
            {:label               (i18n/label :t/node-address)
             :placeholder         (i18n/label :t/wakuv2-node-format)
             :accessibility-label :node-address
             :default-value       address
             :show-cancel         false
             :on-change-text      #(re-frame/dispatch [:wakuv2.ui/input-changed :address %])
             :error               (when (and (not (string/blank? address)) invalid-address?)
                                    (i18n/label :t/invalid-format
                                                {:format (i18n/label :t/wakuv2-node-format)}))
             :bottom-value        0})]]
         (when-not new-node?
           [delete-button id])]]
       [toolbar/toolbar
        {:right
         [quo/button
          {:type     :secondary
           :after    :main-icon/next
           :disabled (not is-valid?)
           :on-press #(re-frame/dispatch [:wakuv2.ui/save-node-pressed])}
          (i18n/label :t/save)]}]])))
