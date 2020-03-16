(ns status-im.ui.screens.wallet.settings.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [status-im.ui.components.styles :as components.styles]
            [status-im.ui.components.list-item.views :as list-item]
            [reagent.core :as reagent]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.search-input.view :as search-input]
            [status-im.ui.components.colors :as colors])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defonce search-active? (reagent/atom false))

(defn toolbar []
  [topbar/topbar
   {:title :t/wallet-assets
    :navigation
    {:icon                :main-icons/back
     :accessibility-label :back-button
     :handler             #(re-frame/dispatch [:wallet.settings.ui/navigate-back-pressed])}}])

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide-sheet])
  (re-frame/dispatch event))

(defn custom-token-actions-view [{:keys [custom?] :as token}]
  (fn []
    [react/view
     [list-item/list-item
      {:theme    :action
       :title    :t/token-details
       :icon     :main-icons/warning
       :on-press #(hide-sheet-and-dispatch
                   [:navigate-to :wallet-custom-token-details token])}]
     (when custom?
       [list-item/list-item
        {:theme    :action-destructive
         :title    :t/remove-token
         :icon     :main-icons/delete
         :on-press #(hide-sheet-and-dispatch
                     [:wallet.custom-token.ui/remove-pressed token])}])]))

(defn render-token
  [{:keys [symbol name icon color custom? checked?] :as token}]
  (reagent/create-class
   {:should-component-update
    (fn [this [_ old-token] [_ new-token]]
      (not= (:checked? old-token) (:checked? new-token)))
    :reagent-render
    (fn [{:keys [symbol name icon color custom? checked?] :as token}]
      [list/list-item-with-checkbox
       {:checked?        checked?
        :on-long-press
        #(re-frame/dispatch
          [:bottom-sheet/show-sheet
           {:content        (custom-token-actions-view token)
            :content-height (if custom? 128 68)}])
        :on-value-change
        #(re-frame/dispatch
          [:wallet.settings/toggle-visible-token (keyword symbol) %])}
       [list/item
        (if icon
          [list/item-image icon]
          [chat-icon/custom-icon-view-list name color])
        [list/item-content
         [list/item-primary name]
         [list/item-secondary symbol]]]])}))

(defn- render-token-wrapper
  [token]
  [render-token token])

(defview manage-assets []
  (letsubs [{search-filter :search-filter
             {custom-tokens true default-tokens nil} :tokens} [:wallet/filtered-grouped-chain-tokens]]
    {:component-will-unmount #(do
                                (re-frame/dispatch [:search/token-filter-changed nil])
                                (reset! search-active? false))}
    [react/view (merge components.styles/flex {:background-color colors/white})
     [toolbar]
     [react/view {:style components.styles/flex}
      [search-input/search-input
       {:search-active? search-active?
        :search-filter search-filter
        :on-cancel #(re-frame/dispatch [:search/token-filter-changed nil])
        :on-focus  (fn [search-filter]
                     (when-not search-filter
                       (re-frame/dispatch [:search/token-filter-changed ""])))
        :on-change (fn [text]
                     (re-frame/dispatch [:search/token-filter-changed text]))}]
      [list/section-list
       {:header
        [react/view {:margin-top 16}
         [list-item/list-item
          {:theme     :action
           :title     :t/add-custom-token
           :icon      :main-icons/add
           :on-press
           #(re-frame/dispatch [:navigate-to :wallet-add-custom-token])}]]
        :sections (concat
                   (when (seq custom-tokens)
                     [{:title (i18n/label :t/custom)
                       :data  custom-tokens}])
                   [{:title (i18n/label :t/default)
                     :data  default-tokens}])
        :key-fn :address
        :stickySectionHeadersEnabled false
        :render-section-header-fn
        (fn [{:keys [title data]}]
          [list-item/list-item {:type :section-header :title title}])
        :render-fn render-token-wrapper}]]]))