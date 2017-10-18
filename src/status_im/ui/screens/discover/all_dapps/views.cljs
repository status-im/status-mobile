(ns status-im.ui.screens.discover.all-dapps.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [cljs.pprint :as pprint]
            [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.screens.discover.components.views :as discover-components]
            [status-im.ui.screens.discover.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.ui.components.toolbar.view :as toolbar]))

(defn navigate-to-dapp [dapp]
  (do (re-frame/dispatch [:set :discover-current-dapp dapp])
      (re-frame/dispatch [:navigate-to :discover-dapp-details])))

(defn render-dapp [{:keys [name photo-path dapp?] :as dapp}]
  [react/touchable-highlight {:on-press #(navigate-to-dapp dapp)}
    [react/view {:style styles/all-dapps-flat-list-item}
      [react/view styles/dapps-list-item-second-row
       [react/view styles/dapps-list-item-name-container
        [react/view styles/dapps-list-item-avatar-container
         [react/view [chat-icon/contact-icon-view dapp {:size 80}]]]
        [react/text {:style           styles/dapps-list-item-name
                     :font            :medium
                     :number-of-lines 2}
         name]]]]])

;; TODO(oskarth): Move this to top level discover ns
(defn preview [dapps]
  [react/view styles/dapp-preview-container
   ;; TODO(oskarth): Refactor to shorter form
   [discover-components/title
    :t/dapps
    :t/all
    #(re-frame/dispatch [:navigate-to :discover-all-dapps])
    true]
   (if (seq dapps)
     [list/flat-list {:data                              (vals dapps)
                      :render-fn                         render-dapp
                      :horizontal                        true
                      :separator?                        false
                      :shows-horizontal-scroll-indicator false
                      :content-container-style           styles/dapp-preview-flat-list}]
     [react/text (i18n/label :t/none)])])

(defview main []
  (letsubs [all-dapps    [:get-all-dapps]
            tabs-hidden? [:tabs-hidden?]]
    (when (seq all-dapps)
      [react/view styles/all-dapps-container
       [toolbar/toolbar {}
        toolbar/default-nav-back
        [toolbar/content-title (i18n/label :t/dapps)]]
       [list/flat-list {:data                    (vals all-dapps)
                        :render-fn               render-dapp
                        :num-columns             3
                        :content-container-style styles/all-dapps-flat-list}]])))
