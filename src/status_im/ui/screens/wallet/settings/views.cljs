(ns status-im.ui.screens.wallet.settings.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.search-input.view :as search-input]
            [status-im.ui.components.colors :as colors]))

(def item-heigth 64)

(defn toolbar []
  [topbar/topbar
   {:title :t/wallet-assets
    :navigation
    {:icon                :main-icons/arrow-left
     :accessibility-label :back-button
     :handler             #(re-frame/dispatch [:wallet.settings.ui/navigate-back-pressed])}}])

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defn custom-token-actions-view [{:keys [custom?] :as token}]
  [react/view
   [quo/list-item
    {:theme    :accent
     :title    (i18n/label :t/token-details)
     :icon     :main-icons/warning
     :on-press #(hide-sheet-and-dispatch
                 [:navigate-to :wallet-custom-token-details token])}]
   (when custom?
     [quo/list-item
      {:theme    :negative
       :title    (i18n/label :t/remove-token)
       :icon     :main-icons/delete
       :on-press #(hide-sheet-and-dispatch
                   [:wallet.custom-token.ui/remove-pressed token])}])])

(defn render-token [{:keys [symbol name icon color checked?] :as token}]
  [quo/list-item {:active        checked?
                  :accessory     :checkbox
                  :animated      false
                  :icon          (if icon
                                   [list/item-image icon]
                                   [chat-icon/custom-icon-view-list name color])
                  :title         name
                  :subtitle      (clojure.core/name symbol)
                  :on-press      #(re-frame/dispatch
                                   [:wallet.settings/toggle-visible-token (keyword symbol) (not checked?)])
                  :on-long-press #(re-frame/dispatch
                                   [:bottom-sheet/show-sheet
                                    {:content [custom-token-actions-view token]}])}])

(defn render-header [{:keys [title]}]
  (when title
    [quo/list-header title]))

(defn header []
  [react/view {:margin-top 16}
   [quo/list-item
    {:theme    :accent
     :title    (i18n/label :t/add-custom-token)
     :icon     :main-icons/add
     :on-press #(re-frame/dispatch [:navigate-to :wallet-add-custom-token])}]])

(defn key-fn [{:keys [address checked?]}]
  (str address checked?))

(defn list-item-layout [_ idx]
  #js {:length item-heigth
       :offset (* item-heigth idx)
       :index  idx})

(defn manage-assets []
  (reagent/with-let [search-active? (reagent/atom false)
                     tokens (re-frame/subscribe [:wallet/filtered-grouped-chain-tokens])]
    (let [{search-filter        :search-filter
           {custom-tokens  true
            default-tokens nil} :tokens} @tokens
          sections                       [{:title (when (seq custom-tokens) (i18n/label :t/custom))
                                           :data  custom-tokens}
                                          {:title (i18n/label :t/default)
                                           :data  default-tokens}]]
      [react/view {:style {:flex             1
                           :background-color colors/white}}
       [toolbar]
       [react/view {:style {:flex 1}}
        [react/view {:padding-horizontal 16
                     :padding-vertical   10}
         [search-input/search-input
          {:search-active? search-active?
           :search-filter  search-filter
           :on-cancel      #(re-frame/dispatch [:search/token-filter-changed nil])
           :on-focus       (fn [search-filter]
                             (when-not search-filter
                               (re-frame/dispatch [:search/token-filter-changed ""])))
           :on-change      (fn [text]
                             (re-frame/dispatch [:search/token-filter-changed text]))}]]
        [list/section-list
         {:header                      [header]
          :sections                    sections
          :key-fn                      key-fn
          :get-item-layout             list-item-layout
          :stickySectionHeadersEnabled false
          :remove-clipped-subviews     false
          :render-section-header-fn    render-header
          :render-fn                   render-token}]]])
    (finally
      (re-frame/dispatch [:search/token-filter-changed nil])
      (reset! search-active? false))))
