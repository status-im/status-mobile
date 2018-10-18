(ns status-im.ui.screens.extensions.views
  (:require-macros [status-im.utils.views :as views])
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar]
            [status-im.ui.components.toolbar.actions :as toolbar.actions]
            [status-im.ui.screens.extensions.styles :as styles]))

(def wnode-icon
  [react/view (styles/wnode-icon true)
   [vector-icons/icon :icons/wnode {:color :white}]])

(defn- render-extension [{:keys [name url active?]}]
  [list/list-item-with-checkbox
   {:checked?        active?
    :on-value-change #(re-frame/dispatch [:extensions.ui/activation-checkbox-pressed name %])}
   [list/item
    wnode-icon
    [list/item-content
     [list/item-primary name]
     [list/item-secondary url]]]])

(views/defview extensions-settings []
  (views/letsubs [extensions [:extensions/all-extensions]]
    [react/view {:flex 1}
     [status-bar/status-bar]
     [toolbar/toolbar {}
      toolbar/default-nav-back
      [toolbar/content-title (i18n/label :t/extensions)]
      [toolbar/actions
       [(toolbar.actions/add false #(re-frame/dispatch [:extensions.ui/add-extension-pressed]))]]]
     [react/view styles/wrapper
      [list/flat-list {:data                    (vals extensions)
                       :default-separator?      false
                       :key-fn                  :id
                       :render-fn               render-extension
                       :content-container-style (merge (when (zero? (count extensions)) {:flex-grow 1}) {:justify-content :center})
                       :empty-component         [react/text {:style styles/empty-list}
                                                 (i18n/label :t/no-extension)]}]]]))
