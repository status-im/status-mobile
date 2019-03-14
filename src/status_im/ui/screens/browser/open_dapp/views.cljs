(ns status-im.ui.screens.browser.open-dapp.views
  (:require
   [re-frame.core :as re-frame]
   [status-im.i18n :as i18n]
   [status-im.ui.components.colors :as colors]
   [status-im.ui.components.react :as react]
   [status-im.ui.components.status-bar.view :as status-bar]
   [status-im.ui.screens.browser.open-dapp.styles :as styles]
   [status-im.ui.components.list.views :as list]
   [status-im.ui.screens.home.views.inner-item :as inner-item]
   [status-im.ui.components.common.common :as components.common]
   [status-im.ui.screens.wallet.components.views :as components]
   [status-im.ui.components.bottom-bar.styles :as tabs.styles]
   [status-im.react-native.resources :as resources]
   [status-im.ui.components.chat-icon.screen :as chat-icon])
  (:require-macros [status-im.utils.views :as views]))

(defn list-item [{:keys [browser-id] :as home-item}]
  [list/deletable-list-item {:type      :browsers
                             :id        browser-id
                             :on-delete #(do
                                           (re-frame/dispatch [:set-swipe-position :browsers browser-id false])
                                           (re-frame/dispatch [:browser.ui/remove-browser-pressed browser-id]))}
   [inner-item/home-list-browser-item-inner-view home-item]])

(defn- render-dapp [{:keys [dapp-url recent? description name] :as dapp}]
  (if recent?
    [list-item dapp]
    [react/touchable-highlight {:on-press #(re-frame/dispatch [:browser.ui/open-dapp-button-pressed dapp-url])}
     [react/view {:style {:padding-top 11 :padding-horizontal 16 :padding-bottom 7 :flex-direction :row}}
      [chat-icon/contact-icon-contacts-tab dapp]
      [react/view {:padding-left 16 :padding-right 2 :flex 1}
       [react/text {:style {:typography :main-medium}} name]
       [react/text {:flex 1}
        [react/text {:style {:font-size  13
                             :color      "#939BA1"
                             :margin-top 5
                             :margin-bottom 2}}
         description]]
       [react/text {:style {:font-size 12
                            :color "#4360DF"}}
        (str dapp-url " ->")]]]]))

(defn list-header [empty?]
  [react/view (when empty? {:flex 1})
   [react/view {:margin             16
                :border-color       colors/gray-lighter
                :border-width       1
                :border-radius      12
                :padding-vertical   16
                :padding-horizontal 44
                :align-items        :center}
    [components.common/image-contain {:container-style {}} {:image (:dapp-store resources/ui) :width 768 :height 333}]
    [react/text {:style {:typography :main-medium
                         :margin-top 12}}
     "Open the ÐApp Store"]
    [react/text {:style {:color     colors/blue
                         :font-size 13}} "https://discover.dapps.eth ->"]]
   (if empty?
     [react/view {:flex 1 :align-items :center :justify-content :center}
      [react/text {:style {:color colors/gray}} "Browsed websites will appear here."]]
     [react/view {:margin-top 14 :margin-left 16 :margin-bottom 4}
      [react/text {:style {:color colors/gray}} (i18n/label :t/recent)]])])

(views/defview open-dapp []
  (views/letsubs [browsers [:browser/browsers-vals]
                  dapps [:contacts/all-dapps]
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
     [list/section-list {:sections                  (cond-> dapps
                                                      (not (empty? browsers))
                                                      (conj {:title (i18n/label :t/recent)
                                                             :data (map #(assoc % :dapp-url (:url %) :recent? true) browsers)}))
                         :key-fn                    :dapp-url
                         :render-fn                 render-dapp
                         :enableEmptySections       true
                         :footer         [react/view
                                          {:style {:height     tabs.styles/tabs-diff
                                                   :align-self :stretch}}]
                         :keyboardShouldPersistTaps :always}]
     ;;TODO next iteration in next PR
     #_(if (empty? browsers)
         [list-header true]
         [list/flat-list {:header         [list-header false]
                          :data           (vals browsers)
                          :footer         [react/view
                                           {:style {:height     tabs.styles/tabs-diff
                                                    :align-self :stretch}}]
                          :key-fn         :browser-id
                          :end-fill-color colors/white
                          :render-fn      list-item}])]))
