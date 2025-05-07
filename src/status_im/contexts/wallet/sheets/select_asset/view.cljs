(ns status-im.contexts.wallet.sheets.select-asset.view
  (:require
    [quo.context]
    [quo.core :as quo]
    [react-native.core :as rn]
    [status-im.contexts.wallet.sheets.select-asset.asset-list.view :as asset-list]
    [status-im.contexts.wallet.sheets.select-asset.style :as style]
    [utils.i18n :as i18n]
    [utils.re-frame :as rf]))

(defn- search-input
  [search-text on-change-text]
  [rn/view {:style style/search-input-container}
   [quo/input
    {:small?         true
     :placeholder    (i18n/label :t/search-assets)
     :icon-name      :i/search
     :value          search-text
     :on-change-text on-change-text}]])

(defn- assets-view
  [{:keys [search-text on-change-text on-select network hide-token-fn disable-token-fn]}]
  [:<>
   [search-input search-text on-change-text]
   [asset-list/view
    {:search-text      search-text
     :on-token-press   (fn [token]
                         (when on-select
                           (on-select token))
                         (rf/dispatch [:hide-bottom-sheet]))
     :network          network
     :hide-token-fn    hide-token-fn
     :disable-token-fn disable-token-fn}]])

(defn- account-view
  [{:keys [emoji name color]}]
  (let [theme (quo.context/use-theme)]
    [quo/context-tag
     {:theme               theme
      :type                :account
      :size                24
      :account-name        name
      :emoji               emoji
      :customization-color color}]))

(defn- network-view
  [{:keys [full-name source]}]
  [quo/context-tag
   {:size         24
    :network-logo source
    :network-name full-name
    :type         :network}])

(defn- provider-view
  [{:keys [name logo-url]}]
  [quo/context-tag
   {:size         24
    :network-logo logo-url
    :network-name name
    :type         :network}])

(defn- subheader-view
  [{:keys [provider account network]}]
  [rn/view {:style style/subheader-container}
   [quo/text
    {:size   :paragraph-2
     :weight :medium}
    (i18n/label :t/select-asset-on)]
   (if provider
     [provider-view provider]
     [account-view account])
   (when provider
     [:<>
      [quo/text
       {:size   :paragraph-2
        :weight :medium}
       (i18n/label :t/to)]
      [account-view account]])
   [quo/text
    {:size   :paragraph-2
     :weight :medium}
    (i18n/label :t/select-asset-via)]
   [network-view network]])

(defn view
  [{:keys [title network provider on-select hide-token-fn disable-token-fn]}]
  (let [account                       (rf/sub [:wallet/current-viewing-account-or-default])
        [search-text set-search-text] (rn/use-state "")
        window-height                 (:height (rn/get-window))]
    [rn/safe-area-view {:style (style/container window-height)}
     [quo/page-nav
      {:type      :no-title
       :icon-name :i/close
       :on-press  (fn []
                    (rf/dispatch [:hide-bottom-sheet]))}]
     [quo/page-top
      {:title                     title
       :title-accessibility-label :title-label}]
     [subheader-view
      {:account  account
       :network  network
       :provider provider}]
     [assets-view
      {:search-text      search-text
       :on-change-text   #(set-search-text %)
       :on-select        on-select
       :network          network
       :hide-token-fn    hide-token-fn
       :disable-token-fn disable-token-fn}]]))
