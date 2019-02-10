(ns status-im.ui.screens.browser.views
  (:require [cljs.tools.reader.edn :as edn]
            [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [status-im.browser.core :as browser]
            [status-im.i18n :as i18n]
            [status-im.ui.components.colors :as colors]
            [status-im.ui.components.icons.vector-icons :as icons]
            [status-im.ui.components.list-selection :as list-selection]
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
            [status-im.ui.screens.wallet.actions :as wallet.actions]
            [status-im.utils.ethereum.core :as ethereum]
            [status-im.utils.http :as http]
            [status-im.utils.js-resources :as js-res]
            [status-im.ui.components.connectivity.view :as connectivity])
  (:require-macros
   [status-im.utils.slurp :refer [slurp]]
   [status-im.utils.views :as views]))

(def browser-config
  (edn/read-string (slurp "./src/status_im/utils/browser_config.edn")))

(defn toolbar-content [url url-original {:keys [secure?]} url-editing?]
  (let [url-text (atom url)]
    [react/view
     [react/view (styles/toolbar-content false)
      [react/touchable-highlight {:on-press #(re-frame/dispatch [:browser.ui/lock-pressed secure?])}
       (if secure?
         [icons/icon :tiny-icons/tiny-lock {:color colors/green}]
         [icons/icon :tiny-icons/tiny-lock-broken])]
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
        [react/touchable-highlight {:style {:flex 1} :on-press #(re-frame/dispatch [:browser.ui/url-input-pressed])}
         [react/text {:style styles/url-text} (http/url-host url-original)]])]]))

(defn- on-options [name browser-id]
  (list-selection/show {:title   name
                        :options (wallet.actions/actions browser-id)}))

(defn toolbar [error? url url-original browser browser-id url-editing?]
  [toolbar.view/toolbar {}
   [toolbar.view/nav-button-with-count
    (actions/close (fn []
                     (re-frame/dispatch [:navigate-to :home])
                     (when error?
                       (re-frame/dispatch [:browser.ui/remove-browser-pressed browser-id]))))]
   [toolbar-content url url-original browser url-editing?]
   [toolbar.view/actions [{:icon      :main-icons/more
                           :icon-opts {:color               :black
                                       :accessibility-label :chat-menu-button}
                           :handler   #(on-options name browser-id)}]]])

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
      (get (:inject-js browser-config) domain-name))))

(defn navigation [url webview can-go-back? can-go-forward?]
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
   [react/touchable-highlight {:on-press #(.reload @webview)
                               :accessibility-label :refresh-page-button}
    [icons/icon :main-icons/refresh]]])

;; should-component-update is called only when component's props are changed,
;; that's why it can't be used in `browser`, because `url` comes from subs
(views/defview browser-component
  [{:keys [webview error? url browser browser-id unsafe? can-go-back?
           can-go-forward? resolving? network-id address url-original
           show-permission show-tooltip opt-in? dapp? rpc-url name]}]
  {:should-component-update (fn [_ _ args]
                              (let [[_ props] args]
                                (not (nil? (:url props)))))}
  [react/view {:flex 1}
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
        :injected-on-start-loading-java-script (str (when-not opt-in? js-res/web3)
                                                    (if opt-in?
                                                      (js-res/web3-opt-in-init (str network-id))
                                                      (js-res/web3-init
                                                       rpc-url
                                                       (ethereum/normalized-address address)
                                                       (str network-id)))
                                                    (get-inject-js url))
        :injected-java-script                  js-res/webview-js}])]
   [navigation url-original webview can-go-back? can-go-forward?]
   [permissions.views/permissions-panel [(:dapp? browser) (:dapp browser)] show-permission]
   (when show-tooltip
     [tooltip/bottom-tooltip-info
      (if (= show-tooltip :secure)
        (i18n/label :t/browser-secure)
        (i18n/label :t/browser-not-secure))
      #(re-frame/dispatch [:browser.ui/close-tooltip-pressed])])])

(views/defview browser []
  (views/letsubs [webview (atom nil)
                  width (reagent/atom nil)
                  {:keys [address settings]} [:account/account]
                  {:keys [browser-id dapp? name unsafe?] :as browser} [:get-current-browser]
                  {:keys [url error? loading? url-editing? show-tooltip show-permission resolving?]} [:get :browser/options]
                  rpc-url    [:get :rpc-url]
                  network-id [:get-network-id]]
    (let [can-go-back?    (browser/can-go-back? browser)
          can-go-forward? (browser/can-go-forward? browser)
          url-original    (browser/get-current-url browser)
          opt-in?         (or (nil? (:web3-opt-in? settings)) (:web3-opt-in? settings))]
      [react/view {:style styles/browser :on-layout #(reset! width (.-width (.-layout (.-nativeEvent %))))}
       [status-bar/status-bar]
       [toolbar error? url url-original browser browser-id url-editing?]
       (when (and loading? (not (nil? @width)))
         [connectivity/loading-indicator @width])
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
                           :address         address
                           :show-permission show-permission
                           :show-tooltip    show-tooltip
                           :opt-in?         opt-in?
                           :rpc-url         rpc-url
                           :name            name}]])))
