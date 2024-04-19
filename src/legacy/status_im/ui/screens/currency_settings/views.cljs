(ns legacy.status-im.ui.screens.currency-settings.views
  (:require-macros [legacy.status-im.utils.views :as views])
  (:require
    [legacy.status-im.ui.components.icons.icons :as icons]
    [legacy.status-im.ui.components.list.views :as list]
    [legacy.status-im.ui.components.react :as react]
    [legacy.status-im.ui.components.search-input.view :as search-input]
    [legacy.status-im.ui.screens.currency-settings.styles :as styles]
    [re-frame.core :as re-frame]
    [reagent.core :as reagent]))

(defonce search-active? (reagent/atom false))

(defn render-currency
  [{:keys [id code display-name]} _ _ current-currency-id]
  (let [selected? (= id current-currency-id)]
    [react/touchable-highlight
     {:on-press            #(re-frame/dispatch [:wallet-legacy.settings.ui/currency-selected id])
      :accessibility-label :currency-item}
     [react/view styles/currency-item
      [react/text {:style styles/currency-name-text}
       (str display-name " (" code ")")]
      (when selected?
        [icons/icon :main-icons/check {:color :active}])]]))

(views/defview currency-settings
  []
  (views/letsubs [currency-id                        [:wallet-legacy/settings-currency]
                  {:keys [currencies search-filter]} [:wallet-legacy/search-filtered-currencies]]
    {:component-will-unmount #(do
                                (re-frame/dispatch [:search/currency-filter-changed nil])
                                (reset! search-active? false))}
    [:<>
     [react/view {:flex 1}
      [react/view
       {:padding-horizontal 16
        :padding-vertical   10}
       [search-input/search-input-old
        {:search-active? search-active?
         :search-filter  search-filter
         :on-cancel      #(re-frame/dispatch [:search/currency-filter-changed nil])
         :on-focus       (fn [search-filter]
                           (when-not search-filter
                             (re-frame/dispatch [:search/currency-filter-changed ""])))
         :on-change      (fn [text]
                           (re-frame/dispatch [:search/currency-filter-changed text]))}]]
      [list/flat-list
       {:data                         (->> currencies
                                           vals
                                           (sort #(compare (:code %1) (:code %2))))
        :key-fn                       :code
        :render-data                  currency-id
        :render-fn                    render-currency
        :keyboard-should-persist-taps :always}]]]))
