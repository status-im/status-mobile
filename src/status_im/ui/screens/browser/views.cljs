(ns status-im.ui.screens.browser.views
  (:require [cljs.tools.reader.edn :as edn]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.browser.core :as browser]
            [status-im.ethereum.core :as ethereum]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.connectivity.view :as connectivity]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.status-bar.view :as status-bar]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.toolbar.actions :as actions]
            [status-im.ui.components.toolbar.view :as toolbar.view]
            [status-im.ui.components.tooltip.views :as tooltip]
            [status-im.ui.components.webview-bridge :as components.webview-bridge]
            [status-im.ui.screens.browser.permissions.views :as permissions.views]
            [status-im.ui.screens.browser.site-blocked.views :as site-blocked.views]
            [status-im.ui.screens.browser.styles :as styles]
            [status-im.utils.http :as http]
            [status-im.utils.js-resources :as js-res])
  (:require-macros
   [status-im.utils.slurp :refer [slurp]]
   [status-im.utils.views :as views]))

(def browser-config-edn
  (slurp "./src/status_im/utils/browser_config.edn"))

(def browser-config
  (memoize
   (fn []
     (edn/read-string (browser-config-edn)))))

(defn toolbar-content [url url-original {:keys [secure?]} url-editing?]
  (let [url-text (atom url)]
    [react/view styles/toolbar-content
     [react/touchable-highlight {:on-press #(re-frame/dispatch [:browser.ui/lock-pressed secure?])}
      (if secure?
        [icons/tiny-icon :tiny-icons/tiny-lock {:color colors/green}]
        [icons/tiny-icon :tiny-icons/tiny-lock-broken])]
     (if url-editing?
       [react/text-input {:on-change-text    #(reset! url-text %)
                          :on-blur           #(re-frame/dispatch [:browser.ui/url-input-blured])
                          :on-submit-editing #(re-frame/dispatch [:browser.ui/url-submitted @url-text])
                          :placeholder       (i18n/label :t/enter-url)
                          :auto-capitalize   :none
                          :auto-correct      false
                          :auto-focus        true
                          :default-value     url
                          :ellipsize         :end
                          :style             styles/url-input}]
       [react/touchable-highlight {:style    styles/url-text-container
                                   :on-press #(re-frame/dispatch [:browser.ui/url-input-pressed])}
        [react/text (http/url-host url-original)]])]))

(defn toolbar [error? url url-original browser browser-id url-editing?]
  [toolbar.view/toolbar
   {:browser? true}
   [toolbar.view/nav-button
    (actions/close (fn []
                     (re-frame/dispatch [:navigate-back])
                     (when error?
                       (re-frame/dispatch [:browser.ui/remove-browser-pressed browser-id]))))]
   [toolbar-content url url-original browser url-editing?]])

(defn- web-view-error [_ code desc]
  (reagent/as-element
   [react/view styles/web-view-error
    [react/i18n-text {:style styles/web-view-error-text :key :web-view-error}]
    [react/text {:style styles/web-view-error-text}
     (str code)]
    [react/text {:style styles/web-view-error-text}
     (str desc)]]))

(defn get-inject-js [url]
  (when url
    (let [domain-name (nth (re-find #"^\w+://(www\.)?([^/:]+)" url) 2)]
      (get (:inject-js (browser-config)) domain-name))))

(defn navigation [browser-id url webview can-go-back? can-go-forward?]
  [react/view styles/navbar
   [react/touchable-highlight {:on-press            #(re-frame/dispatch [:browser.ui/previous-page-button-pressed])
                               :disabled            (not can-go-back?)
                               :style               (when-not can-go-back? styles/disabled-button)
                               :accessibility-label :previous-page-button}
    [react/view
     [icons/icon :main-icons/arrow-left]]]
   [react/touchable-highlight {:on-press            #(re-frame/dispatch [:browser.ui/next-page-button-pressed])
                               :disabled            (not can-go-forward?)
                               :style               (when-not can-go-forward? styles/disabled-button)
                               :accessibility-label :next-page-button}
    [react/view
     [icons/icon :main-icons/arrow-right]]]
   [react/touchable-highlight
    {:on-press #(re-frame/dispatch [:browser.ui/open-modal-chat-button-pressed (http/url-host url)])
     :accessibility-label :modal-chat-button}
    [icons/icon :main-icons/message]]
   [react/touchable-highlight
    {:on-press #(browser/share-link url)
     :accessibility-label :modal-share-link-button}
    [icons/icon :main-icons/share]]
   [react/touchable-highlight {:on-press #(.reload @webview)
                               :accessibility-label :refresh-page-button}
    [icons/icon :main-icons/refresh]]])

;; should-component-update is called only when component's props are changed,
;; that's why it can't be used in `browser`, because `url` comes from subs
(views/defview browser-component
  [{:keys [webview error? url browser browser-id unsafe? can-go-back?
           can-go-forward? resolving? network-id address url-original
           show-permission show-tooltip opt-in? dapp? name]}]
  {:should-component-update (fn [_ _ args]
                              (let [[_ props] args]
                                (not (nil? (:url props)))))}
  [react/view {:flex 1
               :elevation -10}
   [react/view components.styles/flex
    (if unsafe?
      [site-blocked.views/view {:can-go-back? can-go-back?
                                :site         browser-id}]
      [components.webview-bridge/webview-bridge
       {:dapp?                                 dapp?
        :dapp-name                             name
        :ref                                   #(do
                                                  (reset! webview %)
                                                  (re-frame/dispatch [:set :webview-bridge %]))
        :source                                (when (and url (not resolving?)) {:uri url})
        :java-script-enabled                   true
        :bounces                               false
        :local-storage-enabled                 true
        :render-error                          web-view-error
        :on-navigation-state-change            #(re-frame/dispatch [:browser/navigation-state-changed % error?])
        :on-bridge-message                     #(re-frame/dispatch [:browser/bridge-message-received %])
        :on-load                               #(re-frame/dispatch [:browser/loading-started])
        :on-error                              #(re-frame/dispatch [:browser/error-occured])
        :injected-on-start-loading-java-script (str (when-not opt-in? (js-res/web3))
                                                    (if opt-in?
                                                      (js-res/web3-opt-in-init (str network-id))
                                                      (js-res/web3-init address (str network-id)))
                                                    (get-inject-js url))
        :injected-java-script                  (js-res/webview-js)}])]
   [navigation browser-id url-original webview can-go-back? can-go-forward?]
   [permissions.views/permissions-panel [(:dapp? browser) (:dapp browser)] show-permission]
   (when show-tooltip
     [tooltip/bottom-tooltip-info
      (if (= show-tooltip :secure)
        (i18n/label :t/browser-secure)
        (i18n/label :t/browser-not-secure))
      #(re-frame/dispatch [:browser.ui/close-tooltip-pressed])])])

(views/defview browser []
  (views/letsubs [webview (atom nil)
                  window-width [:dimensions/window-width]
                  {:keys [accounts settings]} [:multiaccount]
                  {:keys [browser-id dapp? name unsafe?] :as browser} [:get-current-browser]
                  {:keys [url error? loading? url-editing? show-tooltip show-permission resolving?]} [:browser/options]
                  network-id [:chain-id]]
    (let [can-go-back?    (browser/can-go-back? browser)
          can-go-forward? (browser/can-go-forward? browser)
          url-original    (browser/get-current-url browser)
          opt-in?         (or (nil? (:web3-opt-in? settings)) (:web3-opt-in? settings))]
      [react/view {:style styles/browser}
       [status-bar/status-bar]
       [toolbar error? url url-original browser browser-id url-editing?]
       [react/view
        (when loading?
          [connectivity/loading-indicator window-width])]
       [browser-component {:webview         webview
                           :dapp?           dapp?
                           :error?          error?
                           :url             url
                           :url-original    url-original
                           :browser         browser
                           :browser-id      browser-id
                           :unsafe?         unsafe?
                           :can-go-back?    can-go-back?
                           :can-go-forward? can-go-forward?
                           :resolving?      resolving?
                           :network-id      network-id
                           :address         (:address (ethereum/get-default-account accounts))
                           :show-permission show-permission
                           :show-tooltip    show-tooltip
                           :opt-in?         opt-in?
                           :name            name}]])))
