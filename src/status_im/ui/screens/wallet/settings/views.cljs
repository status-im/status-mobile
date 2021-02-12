(ns status-im.ui.screens.wallet.settings.views
  (:require [re-frame.core :as re-frame]
            [status-im.i18n.i18n :as i18n]
            [status-im.ui.components.chat-icon.screen :as chat-icon]
            [status-im.ui.components.list.views :as list]
            [status-im.ui.components.react :as react]
            [quo.core :as quo]
            [reagent.core :as reagent]
            [status-im.ui.components.topbar :as topbar]
            [status-im.ui.components.search-input.view :as search-input]
            [status-im.ui.components.colors :as colors])
  (:require-macros [status-im.utils.views :refer [defview letsubs]]))

(defonce search-active? (reagent/atom false))

(defn toolbar []
  [topbar/topbar
   {:title (i18n/label :t/wallet-assets)
    :navigation
    {:on-press  #(re-frame/dispatch [:wallet.settings.ui/navigate-back-pressed])}}])

(defn hide-sheet-and-dispatch [event]
  (re-frame/dispatch [:bottom-sheet/hide])
  (re-frame/dispatch event))

(defn custom-token-actions-view [{:keys [custom?] :as token}]
  (fn []
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
                     [:wallet.custom-token.ui/remove-pressed token])}])]))

(defn render-token
  [_]
  (reagent/create-class
   {:should-component-update
    (fn [_ [_ old-token] [_ new-token]]
      (not= (:checked? old-token) (:checked? new-token)))
    :reagent-render
    (fn [{:keys [symbol name icon color checked?] :as token}]
      [quo/list-item
       {:active              checked?
        :accessory           :checkbox
        :animated-accessory? false
        :animated            false
        :icon                (if icon
                               [list/item-image icon]
                               [chat-icon/custom-icon-view-list name color])
        :title               name
        :subtitle            (clojure.core/name symbol)
        :on-press            #(re-frame/dispatch
                               [:wallet.settings/toggle-visible-token (keyword symbol) (not checked?)])
        :on-long-press       #(re-frame/dispatch
                               [:bottom-sheet/show-sheet
                                {:content (custom-token-actions-view token)}])}])}))

(defn- render-token-wrapper
  [token]
  [render-token token])

(defview manage-assets []
  (letsubs [{search-filter                           :search-filter
             {custom-tokens true default-tokens nil} :tokens} [:wallet/filtered-grouped-chain-tokens]]
    {:component-will-unmount #(do
                                (re-frame/dispatch [:search/token-filter-changed nil])
                                (reset! search-active? false))}
    [react/view {:flex 1 :background-color colors/white}
     [toolbar]
     [react/view {:flex 1}
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
       {:header
        [react/view {:margin-top 16}
         [quo/list-item
          {:theme :accent
           :title (i18n/label :t/add-custom-token)
           :icon  :main-icons/add
           :on-press
           #(re-frame/dispatch [:navigate-to :wallet-add-custom-token])}]]
        :sections                    (concat
                                      (when (seq custom-tokens)
                                        [{:title (i18n/label :t/custom)
                                          :data  custom-tokens}])
                                      [{:title (i18n/label :t/default-assets)
                                        :data  default-tokens}])
        :key-fn                      :address
        :stickySectionHeadersEnabled false
        :render-section-header-fn
        (fn [{:keys [title]}]
          [quo/list-header title])
        :render-fn                   render-token-wrapper}]]]))
