(ns status-im.ui.screens.browser.views
  (:require-macros [status-im.utils.slurp :refer [slurp]]
                   [status-im.utils.views :as views])
  (:require [cljs.reader :as reader]
            [re-frame.core :as re-frame]
            [status-im.ui.components.react :as react]
            [status-im.ui.screens.browser.styles :as styles]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.toolbar.view :as toolbar.view]
            [status-im.ui.components.webview-bridge :as components.webview-bridge]
            [status-im.utils.js-resources :as js-res]
            [status-im.ui.components.react :as components]
            [reagent.core :as reagent]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.i18n :as i18n]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.models.browser :as model]
            [status-im.utils.http :as http]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.colors :as colors]
            [clojure.string :as string]
            [status-im.ui.screens.browser.permissions.views :as permissions.views]
            [status-im.utils.platform :as platform]))

(def browser-config
  (reader/read-string (slurp "./src/status_im/utils/browser_config.edn")))

(defn toolbar-content [url {:keys [dapp? history history-index] :as browser} error? url-editing?]
  (let [url-text    (atom url)
        history-url (try (nth history history-index) (catch js/Error _))
        secure?     (or dapp? (and (not error?) (string/starts-with? history-url "https://")))]
    [react/view
     [react/view (styles/toolbar-content false)
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:update-browser-options
                                                                 {:show-tooltip (if secure? :secure :not-secure)}])}
       (if secure?
         [icons/icon :icons/lock {:color colors/green}]
         [icons/icon :icons/lock-opened])]
      (if url-editing?
        [react/text-input {:on-change-text    #(reset! url-text %)
                           :on-blur           #(re-frame/dispatch [:update-browser-options {:url-editing? false}])
                           :on-submit-editing #(do
                                                 (re-frame/dispatch [:update-browser-options {:url-editing? false}])
                                                 (re-frame/dispatch [:update-browser-on-nav-change
                                                                     browser
                                                                     (http/normalize-and-decode-url @url-text)
                                                                     false]))
                           :placeholder       (i18n/label :t/enter-url)
                           :auto-capitalize   :none
                           :auto-correct      false
                           :auto-focus        true
                           :default-value     url
                           :ellipsize         :end
                           :style             styles/url-input}]
        [react/touchable-highlight {:style {:flex 1} :on-press #(re-frame/dispatch [:update-browser-options {:url-editing? true}])}
         [react/text {:style styles/url-text} (http/url-host url)]])]]))

(defn toolbar [webview error? url browser browser-id url-editing?]
  [toolbar.view/toolbar {}
   [toolbar.view/nav-button-with-count
    (actions/close (fn []
                     (when @webview
                       (.sendToBridge @webview "navigate-to-blank"))
                     (re-frame/dispatch [:navigate-back])
                     (when error?
                       (re-frame/dispatch [:remove-browser browser-id]))))]
   [toolbar-content url browser error? url-editing?]
   [toolbar.view/actions [{:icon      :icons/wallet
                           :icon-opts {:color               :black
                                       :accessibility-label :wallet-modal-button}
                           :handler   #(re-frame/dispatch [:navigate-to-modal :wallet-modal])}]]])

(defn- web-view-error [_ code desc]
  (reagent/as-element
   [react/view styles/web-view-error
    [react/i18n-text {:style styles/web-view-error-text :key :web-view-error}]
    [react/text {:style styles/web-view-error-text}
     (str code)]
    [react/text {:style styles/web-view-error-text}
     (str desc)]]))

(defn on-navigation-change [event browser]
  (let [{:strs [url loading]} (js->clj event)]
    (when platform/ios?
      (re-frame/dispatch [:update-browser-options {:loading? loading}]))
    (when (not= "about:blank" url)
      (re-frame/dispatch [:update-browser-on-nav-change browser url loading]))))

(defn get-inject-js [url]
  (let [domain-name (nth (re-find #"^\w+://(www\.)?([^/:]+)" url) 2)]
    (get (:inject-js browser-config) domain-name)))

(defn navigation [webview browser can-go-back? can-go-forward?]
  [react/view styles/toolbar
   [react/touchable-highlight {:on-press            #(re-frame/dispatch [:browser-nav-back browser])
                               :disabled            (not can-go-back?)
                               :style               (when-not can-go-back? styles/disabled-button)
                               :accessibility-label :previou-page-button}
    [react/view
     [icons/icon :icons/arrow-left]]]
   [react/touchable-highlight {:on-press            #(re-frame/dispatch [:browser-nav-forward browser])
                               :disabled            (not can-go-forward?)
                               :style               (merge styles/forward-button
                                                           (when-not can-go-forward? styles/disabled-button))
                               :accessibility-label :next-page-button}
    [react/view
     [icons/icon :icons/arrow-right]]]
   [react/view {:flex 1}]
   [react/touchable-highlight {:on-press #(.reload @webview)}
    [icons/icon :icons/refresh]]])

(views/defview browser []
  (views/letsubs [webview    (atom nil)
                  {:keys [address]} [:get-current-account]
                  {:keys [browser-id dapp? name] :as browser} [:get-current-browser]
                  {:keys [error? loading? url-editing? show-tooltip show-permission]} [:get :browser/options]
                  rpc-url    [:get :rpc-url]
                  network-id [:get-network-id]]
    (let [can-go-back?    (model/can-go-back? browser)
          can-go-forward? (model/can-go-forward? browser)
          url             (model/get-current-url browser)]
      [react/view styles/browser
       [status-bar/status-bar]
       [toolbar webview error? url browser browser-id url-editing?]
       [react/view components.styles/flex
        [components.webview-bridge/webview-bridge
         {:dapp?                                 dapp?
          :dapp-name                             name
          :ref                                   #(do
                                                    (reset! webview %)
                                                    (re-frame/dispatch [:set :webview-bridge %]))
          :source                                {:uri url}
          :java-script-enabled                   true
          :bounces                               false
          :local-storage-enabled                 true
          :render-error                          web-view-error
          :on-navigation-state-change            #(on-navigation-change % browser)
          :on-bridge-message                     #(re-frame/dispatch [:on-bridge-message %])
          :on-load                               #(re-frame/dispatch [:update-browser-options {:error? false}])
          :on-error                              #(re-frame/dispatch [:update-browser-options {:error?   true
                                                                                               :loading? false}])
          :injected-on-start-loading-java-script (str js-res/web3
                                                      (get-inject-js url)
                                                      (js-res/web3-init
                                                       rpc-url
                                                       (ethereum/normalized-address address)
                                                       (str network-id)))
          :injected-java-script                  js-res/webview-js}]
        (when loading?
          [react/view styles/web-view-loading
           [components/activity-indicator {:animating true}]])]
       [navigation webview browser can-go-back? can-go-forward?]
       [permissions.views/permissions-anim-panel browser show-permission]
       (when show-tooltip
         [tooltip/bottom-tooltip-info
          (if (= show-tooltip :secure)
            (i18n/label :t/browser-secure)
            (i18n/label :t/browser-not-secure))
          #(re-frame/dispatch [:update-browser-options {:show-tooltip nil}])])])))
