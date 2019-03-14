(ns status-im.ui.screens.browser.open-dapp.views
  (:require
   [re-frame.core :as re-frame]
   [status-im.i18n :as i18n]
   [status-im.ui.components.colors :as colors]
   [status-im.ui.components.react :as react]
   [status-im.ui.components.status-bar.view :as status-bar]
   [status-im.ui.screens.browser.open-dapp.styles :as styles]
   [status-im.ui.components.list.views :as list]
   [status-im.ui.components.common.common :as components.common]
   [status-im.ui.screens.wallet.components.views :as components]
   [status-im.ui.components.bottom-bar.styles :as tabs.styles]
   [status-im.react-native.resources :as resources]
   [status-im.ui.components.list-item.views :as list-item]
   [status-im.ui.components.icons.vector-icons :as vector-icons])
  (:require-macros [status-im.utils.views :as views]))

(defn list-item [{:keys [browser-id name url]}]
  [list/deletable-list-item {:type      :browsers
                             :id        browser-id
                             :on-delete #(do
                                           (re-frame/dispatch [:set-swipe-position :browsers browser-id false])
                                           (re-frame/dispatch [:browser.ui/remove-browser-pressed browser-id]))}
   [list-item/list-item
    {:on-press #(re-frame/dispatch [:browser.ui/browser-item-selected browser-id])
     :title    name
     :subtitle (or url (i18n/label :t/dapp))
     :image    [react/view styles/browser-icon-container
                [vector-icons/icon :main-icons/browser {:color colors/gray}]]}]])

(def dapp-image-data {:image (:dapp-store resources/ui) :width 768 :height 333})
(def dapp-image (components.common/image-contain nil dapp-image-data))

(defn list-header [empty?]
  [react/view (when empty? {:flex 1})
   [react/touchable-highlight {:on-press #(re-frame/dispatch [:browser.ui/dapp-url-submitted "https://dap.ps"])}
    [react/view styles/dapp-store-container
     [dapp-image nil dapp-image-data]
     [react/text {:style styles/open-dapp-store} (i18n/label :t/open-dapp-store)]
     [react/text {:style {:color colors/blue :font-size 13 :line-height 22}} "https://dap.ps ->"]]]
   (if empty?
     [react/view {:flex 1 :align-items :center :justify-content :center}
      [react/text {:style {:color colors/gray :font-size 15}} (i18n/label :t/browsed-websites)]]
     [react/view {:margin-top 14 :margin-left 16 :margin-bottom 4}
      [react/text {:style {:line-height 22 :font-size 15 :color colors/gray}}
       (i18n/label :t/recent)]])])

(views/defview open-dapp []
  (views/letsubs [browsers [:browser/browsers-vals]
                  url-text (atom nil)]
    [react/keyboard-avoiding-view {:style {:flex 1}}
     [status-bar/status-bar]
     [react/view styles/input-container
      [react/text-input {:on-change-text      #(reset! url-text %)
                         :on-submit-editing   #(re-frame/dispatch [:browser.ui/dapp-url-submitted @url-text])
                         :placeholder         (i18n/label :t/enter-url)
                         :auto-capitalize     :none
                         :auto-correct        false
                         :style               styles/input
                         :accessibility-label :dapp-url-input
                         :return-key-type     :go}]]
     [components/separator]
     (if (empty? browsers)
       [list-header true]
       [list/flat-list {:header         [list-header false]
                        :data           browsers
                        :footer         [react/view
                                         {:style {:height     tabs.styles/tabs-diff
                                                  :align-self :stretch}}]
                        :key-fn         :browser-id
                        :end-fill-color colors/white
                        :render-fn      list-item}])]))