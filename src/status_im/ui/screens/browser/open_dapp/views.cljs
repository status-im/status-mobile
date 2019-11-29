(ns status-im.ui.screens.browser.open-dapp.views
  (:require
   [re-frame.core :as re-frame]
   [status-im.i18n :as i18n]
   [status-im.ui.components.colors :as colors]
   [status-im.ui.components.react :as react]
   [status-im.ui.screens.browser.open-dapp.styles :as styles]
   [status-im.ui.components.list.views :as list]
   [status-im.ui.components.common.common :as components.common]
   [status-im.ui.screens.wallet.components.views :as components]
   [status-im.ui.components.tabbar.styles :as tabs.styles]
   [status-im.react-native.resources :as resources]
   [status-im.ui.components.list-item.views :as list-item]
   [status-im.ui.components.icons.vector-icons :as vector-icons]
   [status-im.ui.components.radio :as radio]
   [status-im.ui.components.icons.vector-icons :as icons]
   [reagent.core :as reagent]
   [status-im.ui.screens.browser.accounts :as accounts])
  (:require-macros [status-im.utils.views :as views]))

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide-sheet])
  (re-frame/dispatch event))

(defn list-item [{:keys [browser-id name url]}]
  [list-item/list-item
   {:on-press      #(re-frame/dispatch [:browser.ui/browser-item-selected browser-id])
    :on-long-press (fn []
                     (re-frame/dispatch
                      [:bottom-sheet/show-sheet
                       {:content        (fn []
                                          [react/view {:flex 1}
                                           [list-item/list-item
                                            {:theme               :action
                                             :title               :t/remove
                                             :accessibility-label :remove-dapp-from-list
                                             :icon                :main-icons/delete
                                             :on-press            #(hide-sheet-and-dispatch [:browser.ui/remove-browser-pressed browser-id])}]
                                           [list-item/list-item
                                            {:theme               :action-destructive
                                             :title               :t/clear-all
                                             :accessibility-label :clear-all-dapps
                                             :icon                :main-icons/delete
                                             :on-press            #(hide-sheet-and-dispatch [:browser.ui/clear-all-browsers-pressed])}]])
                        :content-height 128}]))
    :title         name
    :subtitle      (or url :t/dapp)
    :icon          [react/view styles/browser-icon-container
                    [vector-icons/icon :main-icons/browser {:color colors/gray}]]}])

(def dapp-image-data {:image (resources/get-image :dapp-store) :width 768 :height 333})
(defn dapp-image [] [components.common/image-contain nil dapp-image-data])

(def privacy-otions-visible? (reagent/atom true))

(views/defview privacy-options []
  (views/letsubs [{:keys [settings]} [:multiaccount]]
    (let [privacy-enabled? (or (nil? (:web3-opt-in? settings)) (:web3-opt-in? settings))]
      (when @privacy-otions-visible?
        [react/view styles/privacy-container
         [react/view {:style {:flex-direction :row}}
          [react/view {:flex 1}
           [react/text {:style {:typography :main-medium}} (i18n/label :t/dapps-can-access)]]
          [react/touchable-highlight {:on-press #(reset! privacy-otions-visible? false)}
           [react/view {:style {:width 40 :height 30 :align-items :center :margin-left 20}}
            [react/view {:style styles/close-icon-container}
             [icons/icon :main-icons/close {:color colors/white :width 19 :height 19}]]]]]
         [react/touchable-highlight
          {:on-press (when-not privacy-enabled?
                       #(re-frame/dispatch [:multiaccounts.ui/web3-opt-in-mode-switched (not privacy-enabled?)]))}
          [react/view {:style {:height 56 :justify-content :center :margin-top 8}}
           [react/view {:style {:flex-direction :row :align-items :center}}
            [radio/radio privacy-enabled?]
            [react/text {:style {:margin-left 14}} (i18n/label :t/require-my-permission)]]
           [react/text {:style styles/might-break}
            (i18n/label :t/might-break)]]]
         [react/touchable-highlight
          {:on-press (when privacy-enabled?
                       #(re-frame/dispatch [:multiaccounts.ui/web3-opt-in-mode-switched (not privacy-enabled?)]))}
          [react/view {:style {:flex-direction :row  :height 40 :align-items :center}}
           [radio/radio (not privacy-enabled?)]
           [react/text {:style {:margin-left 14}} (i18n/label :t/always-allow)]]]]))))

(defn list-header [empty?]
  [react/view (when empty? {:flex 1})
   [privacy-options]
   [react/touchable-highlight {:on-press #(re-frame/dispatch [:browser.ui/open-url "https://dap.ps"])}
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

(views/defview select-account []
  (views/letsubs [height [:dimensions/window-height]
                  accounts [:accounts-without-watch-only]
                  {:keys [name color] :as dapps-account} [:dapps-account]]
    [react/view {:position           :absolute
                 :z-index            2
                 :align-items        :center
                 :bottom             (+ 16 tabs.styles/tabs-diff)
                 :left               0
                 :right              0
                 :padding-horizontal 32}
     [react/touchable-highlight
      {:accessibility-label :select-account
       :on-press            #(re-frame/dispatch [:bottom-sheet/show-sheet
                                                 {:content        (accounts/accounts-list accounts dapps-account)
                                                  :content-height (/ height 2)}])}
      [react/view (styles/dapps-account color)
       [icons/icon :main-icons/account {:color colors/white}]
       [react/view {:flex-shrink 1}
        [react/text {:numberOfLines 1
                     :style         {:margin-horizontal 6 :color :white :typography :main-medium}}
         name]]
       [icons/icon :main-icons/dropdown {:color colors/white-transparent}]]]]))

(views/defview open-dapp []
  (views/letsubs [browsers [:browser/browsers-vals]
                  url-text (atom nil)]
    [react/keyboard-avoiding-view {:style {:flex 1}}
     [react/text-input {:on-change-text      #(reset! url-text %)
                        :on-submit-editing   #(re-frame/dispatch [:browser.ui/open-url @url-text])
                        :placeholder         (i18n/label :t/enter-url)
                        :auto-capitalize     :none
                        :auto-correct        false
                        :style               styles/input
                        :accessibility-label :dapp-url-input
                        :return-key-type     :go}]
     [components/separator]
     (if (empty? browsers)
       [list-header true]
       [react/scroll-view
        [list-header false]
        [list/flat-list {:data           browsers
                         :footer         [react/view
                                          {:style {:height     (+ tabs.styles/tabs-diff 64)
                                                   :align-self :stretch}}]
                         :key-fn         :browser-id
                         :end-fill-color colors/white
                         :render-fn      list-item}]])
     [select-account]]))
