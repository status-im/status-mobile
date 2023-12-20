(ns legacy.status-im.ui.screens.wallet.settings.views
  (:require
    [legacy.status-im.ui.components.chat-icon.screen :as chat-icon]
    [legacy.status-im.ui.components.colors :as colors]
    [legacy.status-im.ui.components.core :as quo]
    [legacy.status-im.ui.components.list.item :as list.item]
    [legacy.status-im.ui.components.list.views :as list]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.search-input.view :as search-input]
    [legacy.status-im.ui.components.topbar :as topbar]
    [legacy.status-im.ui.screens.wallet.components.views :as wallet.components]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]
    [utils.i18n :as i18n])
  (:require-macros [legacy.status-im.utils.views :refer [defview letsubs]]))

(defonce search-active? (reagent/atom false))

(defn toolbar
  []
  [topbar/topbar
   {:title (i18n/label :t/wallet-assets)
    :navigation
    {:on-press #(re-frame/dispatch [:wallet-legacy.settings.ui/navigate-back-pressed])}}])

(defn hide-sheet-and-dispatch
  [event]
  (re-frame/dispatch [:bottom-sheet/hide-old])
  (re-frame/dispatch event))

(defn custom-token-actions-view
  [{:keys [custom?] :as token}]
  (fn []
    [react/view
     [list.item/list-item
      {:theme    :accent
       :title    (i18n/label :t/token-details)
       :icon     :main-icons/warning
       :on-press #(hide-sheet-and-dispatch
                   [:navigate-to :wallet-custom-token-details token])}]
     (when custom?
       [list.item/list-item
        {:theme    :negative
         :title    (i18n/label :t/remove-token)
         :icon     :main-icons/delete
         :on-press #(hide-sheet-and-dispatch
                     [:wallet-legacy.custom-token.ui/remove-pressed token])}])]))

(defn render-token
  [_]
  (reagent/create-class
   {:should-component-update
    (fn [_ [_ old-token] [_ new-token]]
      (not= (:checked? old-token) (:checked? new-token)))
    :reagent-render
    (fn [{sym      :symbol
          title    :name
          icon     :icon
          color    :color
          checked? :checked?
          :as      token}]
      [list.item/list-item
       {:active              checked?
        :accessory           :checkbox
        :animated-accessory? false
        :animated            false
        :icon                (if icon
                               [wallet.components/token-icon icon]
                               [chat-icon/custom-icon-view-list title color])
        :title               title
        :subtitle            (name sym)
        :on-press            #(re-frame/dispatch [:wallet-legacy.settings/toggle-visible-token
                                                  (keyword sym)
                                                  (not checked?)])
        :on-long-press       #(re-frame/dispatch [:bottom-sheet/show-sheet-old
                                                  {:content (custom-token-actions-view token)}])}])}))

(defn- render-token-wrapper
  [token]
  [render-token token])

(defview manage-assets
  []
  (letsubs [{search-filter                           :search-filter
             {custom-tokens true default-tokens nil} :tokens}
            [:wallet-legacy/filtered-grouped-chain-tokens]]
    {:component-will-unmount #(do
                                (re-frame/dispatch [:wallet-legacy/search-token-filter-changed nil])
                                (reset! search-active? false))}
    [react/view {:flex 1 :background-color colors/white}
     [toolbar]
     [react/view {:flex 1}
      [react/view
       {:padding-horizontal 16
        :padding-vertical   10}
       [search-input/search-input-old
        {:search-active? search-active?
         :search-filter  search-filter
         :on-cancel      #(re-frame/dispatch [:wallet-legacy/search-token-filter-changed nil])
         :on-focus       (fn [search-filter]
                           (when-not search-filter
                             (re-frame/dispatch [:wallet-legacy/search-token-filter-changed ""])))
         :on-change      (fn [text]
                           (re-frame/dispatch [:wallet-legacy/search-token-filter-changed text]))}]]
      [list/section-list
       {:header
        [react/view {:margin-top 16}
         [list.item/list-item
          {:theme :accent
           :title (i18n/label :t/add-custom-token)
           :icon :main-icons/add
           :on-press
           #(re-frame/dispatch [:navigate-to :wallet-add-custom-token])}]]
        :sections (concat
                   (when (seq custom-tokens)
                     [{:title (i18n/label :t/custom)
                       :data  custom-tokens}])
                   [{:title (i18n/label :t/default-assets)
                     :data  default-tokens}])
        :key-fn :address
        :stickySectionHeadersEnabled false
        :render-section-header-fn
        (fn [{:keys [title]}]
          [quo/list-header title])
        :render-fn render-token-wrapper}]]]))
