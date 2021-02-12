(ns status-im.ui.screens.browser.tabs.views
  (:require-macros [status-im.utils.views :as views])
  (:require [status-im.ui.components.react :as react]
            [status-im.ui.components.topbar :as topbar]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.plus-button :as components.plus-button]
            [status-im.ui.components.list.views :as list]
            [quo.core :as quo]
            [status-im.ui.components.icons.icons :as icons]
            [status-im.ui.screens.wallet.components.views :as components]
            [re-frame.core :as re-frame]
            [status-im.utils.http :as http]
            [reagent.core :as reagent]))

(defn list-item [_]
  (let [loaded (reagent/atom nil)]
    (fn [{:keys [browser-id name url empty-tab]}]
      [react/view {:flex-direction :row :flex 1}
       [react/view {:flex 1}
        [quo/list-item
         {:on-press #(if empty-tab
                       (re-frame/dispatch [:browser.ui/open-empty-tab])
                       (re-frame/dispatch [:browser.ui/browser-item-selected browser-id]))
          :title    name
          :subtitle (when-not empty-tab (or url (i18n/label :t/dapp)))
          :accessibility-label (keyword (str "tab-item" name))
          :icon     [react/view {:width            40
                                 :height           40
                                 :align-items      :center
                                 :justify-content  :center}
                     (when (or (nil? @loaded)  @loaded)
                       [react/image {:onLoad #(reset! loaded true)
                                     :style  {:width 32 :height 32 :position :absolute :top 4 :left 4}
                                     :source {:uri (str "https://" (http/url-host url) "/favicon.ico")}}])
                     (when-not @loaded
                       [react/view {:width            40
                                    :height           40
                                    :border-radius    20
                                    :background-color colors/gray-lighter
                                    :align-items      :center
                                    :justify-content  :center}
                        [icons/icon :main-icons/browser {:color colors/gray}]])]}]]
       (when-not empty-tab
         [react/touchable-highlight
          {:style    {:width 60 :justify-content :center :align-items :center}
           :accessibility-label :empty-tab
           :on-press #(re-frame/dispatch [:browser.ui/remove-browser-pressed browser-id])}
          [icons/icon :main-icons/close-circle {:color colors/gray}]])])))

(views/defview tabs []
  (views/letsubs [browsers [:browser/browsers-vals]]
    [react/view {:flex 1}
     [topbar/topbar
      {:modal?        true
       :border-bottom false
       :navigation    :none
       :right-accessories
       [{:label    (i18n/label :t/close-all)
         :accessibility-label :close-all
         :on-press #(do (re-frame/dispatch [:browser.ui/clear-all-browsers-pressed])
                        (re-frame/dispatch [:browser.ui/open-empty-tab]))}]
       :title         (i18n/label :t/tabs)}]
     [components/separator-dark]
     [list/flat-list {:data      (conj browsers {:empty-tab true
                                                 :name      (i18n/label :t/empty-tab)
                                                 :url       ""})
                      :footer    [react/view
                                  {:style {:height     64
                                           :align-self :stretch}}]
                      :key-fn    :browser-id
                      :render-fn list-item}]

     [components.plus-button/plus-button
      {:on-press #(re-frame/dispatch [:browser.ui/open-empty-tab])}]]))