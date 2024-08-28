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
     :chain-ids        [(:chain-id network)]
     :hide-token-fn    hide-token-fn
     :disable-token-fn disable-token-fn}]])

(defn- account-view
  [{:keys [emoji name color]}]
  (let [theme (quo.theme/use-theme)]
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

(defn- subheader-view
  [{:keys [account network]}]
  [rn/view {:style style/subheader-container}
   [quo/text
    {:size   :paragraph-2
     :weight :medium}
    (i18n/label :t/select-asset-on)]
   [account-view account]
   [quo/text
    {:size   :paragraph-2
     :weight :medium}
    (i18n/label :t/select-asset-via)]
   [network-view network]])

(defn view
  [{:keys [title on-select hide-token-fn disable-token-fn]}]
  (let [account                       (rf/sub [:wallet/current-viewing-account])
        {:keys [network]}             (rf/sub [:wallet/swap])
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
      {:account account
       :network network}]
     [assets-view
      {:search-text      search-text
       :on-change-text   #(set-search-text %)
       :on-select        on-select
       :network          network
       :hide-token-fn    hide-token-fn
       :disable-token-fn disable-token-fn}]]))
