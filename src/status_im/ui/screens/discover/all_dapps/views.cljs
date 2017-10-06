(ns status-im.ui.screens.discover.all-dapps.views
  (:require-macros [status-im.utils.views :refer [defview letsubs]])
  (:require [cljs.pprint :as pprint]
            [re-frame.core :as re-frame]
            [status-im.components.react :as react]
            [status-im.components.list.views :as list]
            [status-im.components.chat-icon.screen :as chat-icon]
            [status-im.components.carousel.carousel :as carousel]
            [status-im.ui.screens.discover.components.views :as discover-components]
            [status-im.ui.screens.discover.styles :as styles]
            [status-im.i18n :as i18n]
            [status-im.components.toolbar-new.view :as toolbar]
            [taoensso.timbre :as log]))

(defn navigate-to-dapp [dapp]
  (do (re-frame/dispatch [:set :discover-current-dapp dapp])
      (re-frame/dispatch [:navigate-to :discover-dapp-details])))

(defn render-dapp [{:keys [name photo-path dapp?] :as dapp}]
  [react/touchable-highlight {:on-press #(navigate-to-dapp dapp)}
    [react/view {:style styles/all-dapps-flat-list-item}
      [react/view styles/dapps-list-item-second-row
       [react/view styles/dapps-list-item-name-container
        [react/view styles/dapps-list-item-avatar-container
         [react/view (chat-icon/contact-icon-contacts-tab dapp)]]
        [react/text {:style           styles/dapps-list-item-name
                     :font            :medium
                     :number-of-lines 1}
         name]]]]])

;; TODO(oskarth): Carousel task, possibly different subcomponent
(def dapp-item render-dapp)

;; TODO(oskarth): Move this to top level discover ns
(defn preview [dapps]
  [react/view styles/dapp-preview-container
   ;; TODO(oskarth): Refactor to shorter form
   [discover-components/title
    :t/dapps
    :t/all
    #(re-frame/dispatch [:navigate-to :discover-all-dapps])]
   (if (seq dapps)
     ;; TODO(oskarth): Make this carousel show more dapps at a time
     [carousel/carousel {:page-style styles/carousel-page-style
                         :count      (count dapps)}
      (for [[_ dapp] dapps]
        ^{:key (str (:name dapp))}
        [react/touchable-highlight {:on-press #(navigate-to-dapp dapp)}
         [react/view styles/dapp-preview-content
          [dapp-item dapp]]])]
     [react/text (i18n/label :t/none)])])

(defview main []
  (letsubs [all-dapps    [:get-all-dapps]
            tabs-hidden? [:tabs-hidden?]]
    (when (seq all-dapps)
      [react/view styles/all-dapps-container
       [toolbar/toolbar2 {}
        toolbar/default-nav-back
        ;; TODO(oskarth): Lowercase and wrong style for some reason
        [react/view [react/text :t/dapps]]]
       [list/flat-list {:data                    (vals all-dapps)
                        :render-fn               render-dapp
                        :num-columns             3
                        :content-container-style styles/all-dapps-flat-list}]])))
