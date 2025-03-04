(ns status-im.contexts.wallet.sheets.select-asset.view
  (:require
    [quo.core :as quo]
    [quo.theme]
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
  [{:keys [search-text on-change-text on-select network address hide-token-fn disable-token-fn]}]
  [:<>
   [search-input search-text on-change-text]
   [asset-list/view
    {:search-text      search-text
     :on-token-press   (fn [token]
                         (when on-select
                           (on-select token))
                         (rf/dispatch [:hide-bottom-sheet]))
     :network          network
     :address          address
     :hide-token-fn    hide-token-fn
     :disable-token-fn disable-token-fn}]])

(defn- account-view
  [address]
  (let [theme                      (quo.theme/use-theme)
        {:keys [name emoji color]} (rf/sub [:wallet/account-by-address address])]
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
  [{:keys [provider address network]}]
  [rn/view {:style style/subheader-container}
   [quo/text
    {:size   :paragraph-2
     :weight :medium}
    (i18n/label :t/select-asset-on)]
   (if provider
     [provider-view provider]
     [account-view address])
   (when provider
     [:<>
      [quo/text
       {:size   :paragraph-2
        :weight :medium}
       (i18n/label :t/to)]
      [account-view address]])
   [quo/text
    {:size   :paragraph-2
     :weight :medium}
    (i18n/label :t/select-asset-via)]
   [network-view network]])

(defn view
  [{:keys [address title network provider on-select hide-token-fn disable-token-fn]}]
  (let [account-address               (or address
                                          (rf/sub [:wallet/current-viewing-account-address-or-default]))
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
      {:address  account-address
       :network  network
       :provider provider}]
     [assets-view
      {:search-text      search-text
       :on-change-text   #(set-search-text %)
       :on-select        on-select
       :network          network
       :address          account-address
       :hide-token-fn    hide-token-fn
       :disable-token-fn disable-token-fn}]]))
