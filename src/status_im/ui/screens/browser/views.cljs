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
            [status-im.ui.components.chat-icon.screen :as chat-icon.screen]
            [status-im.ui.components.icons.vector-icons :as vector-icons]
            [status-im.i18n :as i18n]))

(views/defview toolbar-content-dapp [contact-identity]
  (views/letsubs [contact [:get-contact-by-identity contact-identity]]
    [react/view
     [react/view styles/toolbar-content-dapp
      [chat-icon.screen/dapp-icon-browser contact 36]
      [react/view styles/dapp-name
       [react/text {:style               styles/dapp-name-text
                    :number-of-lines     1
                    :font                :toolbar-title
                    :accessibility-label :dapp-name-text}
        (:name contact)]
       [react/text {:style styles/dapp-text}
        (i18n/label :t/dapp)]]]]))

(def browser-config
  (reader/read-string (slurp "./src/status_im/utils/browser_config.edn")))

(defn toolbar-content [{:keys [url] :as browser}]
  (let [url-text (atom nil)]
    [react/view
     [react/view (styles/toolbar-content false)
      [react/text-input {:on-change-text    #(reset! url-text %)
                         :on-submit-editing #(re-frame/dispatch [:update-browser (assoc browser :url @url-text)])
                         :auto-focus        (not url)
                         :placeholder       (i18n/label :t/enter-url)
                         :auto-capitalize   :none
                         :auto-correct      false
                         :default-value     url
                         :style             styles/url-input}]
      ;;TODO .reload doesn't work, implement later
      #_[react/touchable-highlight {:on-press #(when @webview (.reload @webview))}
         [react/view
          [vector-icons/icon :icons/refresh]]]]]))

(defn web-view-error []
  (reagent/as-element
    [react/view styles/web-view-error
     [react/text (i18n/label :t/web-view-error)]]))

(defn web-view-loading []
  (reagent/as-element
    [react/view styles/web-view-loading
     [components/activity-indicator {:animating true}]]))

(defn on-navigation-change [event browser]
  (let [{:strs [url title canGoBack canGoForward]} (js->clj event)]
    (when-not (= "about:blank" url)
      (re-frame/dispatch [:update-browser (assoc browser :url url :name title)]))
    (re-frame/dispatch [:update-browser-options {:can-go-back? canGoBack :can-go-forward? canGoForward}])))

(defn get-inject-js [url]
  (let [domain-name (nth (re-find #"^\w+://(www\.)?([^/:]+)" url) 2)]
    (get (:inject-js browser-config) domain-name)))

(views/defview browser []
  (views/letsubs [webview (atom nil)
                  {:keys [dapp? contact url] :as browser} [:get-current-browser]
                  {:keys [can-go-back? can-go-forward?]} [:get :browser/options]
                  extra-js [:web-view-extra-js]
                  rpc-url [:get :rpc-url]
                  unread-messages-number [:get-chats-unread-messages-number]]
    [react/keyboard-avoiding-view styles/browser
     [status-bar/status-bar]
     [toolbar.view/toolbar {}
      toolbar.view/nav-back-count
      (if dapp?
        [toolbar-content-dapp contact unread-messages-number]
        [toolbar-content browser unread-messages-number])]
     (if url
       [components.webview-bridge/webview-bridge
        {:ref                                   #(reset! webview %)
         :source                                {:uri url}
         :java-script-enabled                   true
         :bounces                               false
         :local-storage-enabled                 true
         :start-in-loading-state                true
         :render-error                          web-view-error
         :render-loading                        web-view-loading
         :on-navigation-state-change            #(on-navigation-change % browser)
         :injected-on-start-loading-java-script (str js-res/web3
                                                     js-res/jquery
                                                     (get-inject-js url)
                                                     (js-res/web3-init rpc-url))
         :injected-java-script                  (str js-res/webview-js extra-js)}]
       [react/view styles/background
        [react/text (i18n/label :t/enter-dapp-url)]])
     [react/view styles/toolbar
      [react/touchable-highlight {:on-press            #(.goBack @webview)
                                  :disabled            (not can-go-back?)
                                  :accessibility-label :previou-page-button}
       [react/view (when (not can-go-back?) {:opacity 0.4})
        [vector-icons/icon :icons/arrow-left]]]
      [react/touchable-highlight {:on-press            #(.goForward @webview)
                                  :disabled            (not can-go-forward?)
                                  :style               styles/forward-button
                                  :accessibility-label :next-page-button}
       [react/view (when (not can-go-forward?) {:opacity 0.4})
        [vector-icons/icon :icons/arrow-right]]]]]))
