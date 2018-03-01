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
  [react/touchable-highlight {:on-press #(navigate-to-dapp dapp)
                              :disabled  (empty? name)}
    [react/view {:style styles/all-dapps-flat-list-item}

       [react/view styles/dapps-list-item-name-container
        [react/view styles/dapps-list-item-avatar-container
         [react/view [chat-icon/contact-icon-view dapp {:size 80}]]]
        [react/text {:style           styles/dapps-list-item-name
                     :font            :medium
                     :number-of-lines 2}
         name]]]])

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
                      :key-fn                            :dapp-url
                      :render-fn                         render-dapp
                      :horizontal                        true
                      :default-separator?                false
                      :shows-horizontal-scroll-indicator false
                      :content-container-style           styles/dapp-preview-flat-list}]
     [react/text (i18n/label :t/none)])])

;; todo(goranjovic): this is a hacky fix for the dapp alignment problem in a flatlist based grid
;; it works fine only if the number of items is evenly divisible with the number of columns
;; so we make it so by adding up blank dapp items.
;; the proper solution might be to find a decent component for grid lists
(defn add-blank-dapps-for-padding [columns dapps]
  (let [extras (mod (count dapps) columns)]
    (if (zero? extras)
      dapps
      (concat dapps
              (map (fn [i] {:name ""
                            :dapp-url (str "blank-" i)})
                   (range (- columns extras)))))))

(defview main []
  (letsubs [all-dapps    [:discover/all-dapps]]
    (let [columns 3]
      (when (seq all-dapps)
        [react/view styles/all-dapps-container
         [toolbar/toolbar {}
          toolbar/default-nav-back
          [toolbar/content-title (i18n/label :t/dapps)]]
         [list/flat-list {:data                    (add-blank-dapps-for-padding columns (vals all-dapps))
                          :key-fn                  :dapp-url
                          :render-fn               render-dapp
                          :num-columns             columns
                          :content-container-style styles/all-dapps-flat-list}]]))))
